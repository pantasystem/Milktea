package net.pantasystem.milktea.data.infrastructure.user

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.misskey.v12.user.reaction.UserReactionRequest
import net.pantasystem.milktea.app_store.user.UserReactionPagingStore
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.note.NoteRelationGetter
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.reaction.UserReaction
import net.pantasystem.milktea.model.user.reaction.UserReactionRelation
import javax.inject.Inject
import javax.inject.Singleton
import net.pantasystem.milktea.api.misskey.v12.user.reaction.UserReaction as UserReactionDTO

class UserReactionPagingStoreImpl(
    private val pagingImpl: UserReactionPagingImpl,
) : UserReactionPagingStore {

    class Factory @Inject constructor(
        val factory: UserReactionPagingImpl.Factory
    ) : UserReactionPagingStore.Factory {
        override fun create(userId: User.Id): UserReactionPagingStore {
            return UserReactionPagingStoreImpl(factory.create(userId))
        }
    }

    private val previousPagingController = PreviousPagingController(
        pagingImpl,
        pagingImpl,
        pagingImpl,
        pagingImpl
    )

    override val state: Flow<PageableState<List<UserReactionRelation>>>
        get() = pagingImpl.state

    override suspend fun clear(): Result<Unit> = runCancellableCatching {
        pagingImpl.mutex.withLock {
            pagingImpl.setState(PageableState.Loading.Init())
        }
    }

    override suspend fun loadPrevious(): Result<Unit> = runCancellableCatching {
        previousPagingController.loadPrevious().getOrThrow()
    }
}


class UserReactionPagingImpl(
    val userId: User.Id,
    val accountRepository: AccountRepository,
    val userDataSource: UserDataSource,
    val noteDataSourceAdder: NoteDataSourceAdder,
    val noteRelationGetter: NoteRelationGetter,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val userDTOEntityConverter: UserDTOEntityConverter,
) : StateLocker, PreviousLoader<UserReactionDTO>,
    PaginationState<UserReactionRelation>, EntityConverter<UserReactionDTO, UserReactionRelation>,
    IdGetter<String> {

    @Singleton
    class Factory @Inject constructor(
        val userDataSource: UserDataSource,
        val noteDataSourceAdder: NoteDataSourceAdder,
        val noteRelationGetter: NoteRelationGetter,
        val misskeyAPIProvider: MisskeyAPIProvider,
        val accountRepository: AccountRepository,
        val userDTOEntityConverter: UserDTOEntityConverter,
    ){
        fun create(userId: User.Id): UserReactionPagingImpl {
            return UserReactionPagingImpl(
                userId,
                userDataSource = userDataSource,
                noteDataSourceAdder = noteDataSourceAdder,
                misskeyAPIProvider = misskeyAPIProvider,
                accountRepository = accountRepository,
                noteRelationGetter = noteRelationGetter,
                userDTOEntityConverter = userDTOEntityConverter,
            )
        }
    }
    override val mutex: Mutex = Mutex()

    private val _state = MutableStateFlow<PageableState<List<UserReactionRelation>>>(
        PageableState.Loading.Init()
    )
    override val state: Flow<PageableState<List<UserReactionRelation>>>
        get() = _state


    override suspend fun loadPrevious(): Result<List<UserReactionDTO>> = runCancellableCatching {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        (misskeyAPIProvider.get(account)).getUserReactions(
            UserReactionRequest(
                i = account.token,
                untilId = getUntilId(),
                limit = 20,
                userId = userId.id
            )
        ).throwIfHasError().body()!!
    }

    override suspend fun getSinceId(): String? {
        return null
    }

    override suspend fun getUntilId(): String? {
        return (_state.value.content as? StateContent.Exist)?.rawContent?.lastOrNull()?.reaction?.id?.serverId

    }

    override fun getState(): PageableState<List<UserReactionRelation>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<UserReactionRelation>>) {
        _state.value = state
    }

    override suspend fun convertAll(list: List<UserReactionDTO>): List<UserReactionRelation> {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return list.map { userReaction ->
            val user = userDTOEntityConverter.convert(account, userReaction.user, false)
            userDataSource.add(user)
            val note = noteDataSourceAdder.addNoteDtoToDataSource(account, userReaction.note)
            UserReactionRelation(
                reaction = UserReaction(
                    id = UserReaction.Id(accountId = account.accountId, serverId = userReaction.id),
                    type = userReaction.type,
                    noteId = note.id,
                    userId = user.id,
                    createdAt = userReaction.createdAt,
                ),
                note = noteRelationGetter.get(note).getOrThrow(),
                user = user
            )
        }
    }
}

