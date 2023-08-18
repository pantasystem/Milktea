package net.pantasystem.milktea.data.infrastructure.notes.renote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.misskey.notes.FindRenotes
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.common.MastodonLinkHeaderDecoder
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.EntityConverter
import net.pantasystem.milktea.common.paginator.IdGetter
import net.pantasystem.milktea.common.paginator.PaginationState
import net.pantasystem.milktea.common.paginator.PreviousLoader
import net.pantasystem.milktea.common.paginator.PreviousPagingController
import net.pantasystem.milktea.common.paginator.StateLocker
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.repost.RenoteType
import net.pantasystem.milktea.model.note.repost.RenotesPagingService
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject


class RenotesPagingServiceImpl(
    targetNoteId: Note.Id,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val mastodonAPIProvider: MastodonAPIProvider,
    val accountRepository: AccountRepository,
    val noteDataSourceAdder: NoteDataSourceAdder,
    val userDataSource: UserDataSource,
    ) : RenotesPagingService {

    class Factory @Inject constructor(
        val misskeyAPIProvider: MisskeyAPIProvider,
        val accountRepository: AccountRepository,
        val noteDataSourceAdder: NoteDataSourceAdder,
        val mastodonAPIProvider: MastodonAPIProvider,
        val userDataSource: UserDataSource,
    ) : RenotesPagingService.Factory {
        override fun create(noteId: Note.Id): RenotesPagingService {
            return RenotesPagingServiceImpl(
                noteId,
                misskeyAPIProvider,
                mastodonAPIProvider,
                accountRepository,
                noteDataSourceAdder,
                userDataSource,
            )
        }
    }
    private val pagingImpl = RenotesPagingImpl(
        targetNoteId,
        misskeyAPIProvider,
        mastodonAPIProvider,
        accountRepository,
        noteDataSourceAdder,
        userDataSource
    )
    private val controller =
        PreviousPagingController(pagingImpl, pagingImpl, pagingImpl, pagingImpl)

    override val state: Flow<PageableState<List<RenoteType>>>
        get() = pagingImpl.state

    override suspend fun clear() {
        pagingImpl.mutex.withLock {
            pagingImpl.setState(PageableState.Fixed(StateContent.NotExist()))
        }
    }

    override suspend fun next() {
        controller.loadPrevious()
    }

    override suspend fun refresh() {
        this.clear()
        this.next()
    }
}

class RenotesPagingImpl(
    private val targetNoteId: Note.Id,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val mastodonAPIProvider: MastodonAPIProvider,
    val accountRepository: AccountRepository,
    val noteDataSourceAdder: NoteDataSourceAdder,
    val userDataSource: UserDataSource,
) : PreviousLoader<RenoteNetworkDTO>,
    EntityConverter<RenoteNetworkDTO, RenoteType>,
    StateLocker,
    PaginationState<RenoteType>,
    IdGetter<String> {

    private val _state: MutableStateFlow<PageableState<List<RenoteType>>> =
        MutableStateFlow(PageableState.Fixed(StateContent.NotExist()))
    override val state: Flow<PageableState<List<RenoteType>>>
        get() = _state

    override val mutex: Mutex = Mutex()

    private var maxId: String? = null
    private var minId: String? = null

    override suspend fun loadPrevious(): Result<List<RenoteNetworkDTO>> {
        return runCancellableCatching {
            val account = accountRepository.get(targetNoteId.accountId).getOrThrow()
            val i = account.token
            when(account.instanceType) {
                Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                    misskeyAPIProvider.get(account.normalizedInstanceUri)
                        .renotes(FindRenotes(i = i, noteId = targetNoteId.noteId, untilId = getUntilId()))
                        .throwIfHasError().body()!!.let { list ->
                            list.map {
                                RenoteNetworkDTO.Renote(it)
                            }
                        }
                }
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    val untilId = getUntilId()
                    val empty = (getState().content as? StateContent.Exist)?.rawContent.isNullOrEmpty()
                    if (untilId == null && !empty) {
                        return@runCancellableCatching emptyList()
                    }
                    val res = mastodonAPIProvider.get(account)
                        .getRebloggedBy(targetNoteId.noteId, maxId = getUntilId())
                        .throwIfHasError()
                    MastodonLinkHeaderDecoder(res.headers()["Link"]).let {
                        maxId = it.getMaxId()
                    }
                    res.body()!!.let { list ->
                        list.map {
                            RenoteNetworkDTO.Reblog(it)
                        }
                    }
                }
            }

        }
    }

    override suspend fun convertAll(list: List<RenoteNetworkDTO>): List<RenoteType> {
        val account = accountRepository.get(targetNoteId.accountId).getOrThrow()
        return list.map {
            when(it) {
                is RenoteNetworkDTO.Reblog -> {
                    val model = it.accountDTO.toModel(account = account)
                    userDataSource.add(model).getOrThrow()
                    RenoteType.Reblog(model.id)
                }
                is RenoteNetworkDTO.Renote -> {
                    val note = noteDataSourceAdder.addNoteDtoToDataSource(account, it.note)
                    RenoteType.Renote(note.id, isQuote = note.isQuote())
                }
            }
        }
    }

    override suspend fun getSinceId(): String? {
        if (minId != null) {
            return maxId
        }
        return ((getState().content as? StateContent.Exist)?.rawContent?.firstOrNull() as? RenoteType.Renote)?.noteId?.noteId
    }

    override suspend fun getUntilId(): String? {
        if (maxId != null) {
            return maxId
        }
        return ((getState().content as? StateContent.Exist)?.rawContent?.lastOrNull() as? RenoteType.Renote)?.noteId?.noteId
    }

    override fun getState(): PageableState<List<RenoteType>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<RenoteType>>) {
        _state.value = state
    }


}


sealed interface RenoteNetworkDTO {
    data class Renote(val note: NoteDTO) : RenoteNetworkDTO

    data class Reblog(val accountDTO: MastodonAccountDTO) : RenoteNetworkDTO
}