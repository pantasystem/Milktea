package net.pantasystem.milktea.data.infrastructure.user

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountRelationshipDTO
import net.pantasystem.milktea.api.misskey.users.RequestUser
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.api.misskey.v10.MisskeyAPIV10
import net.pantasystem.milktea.api.misskey.v10.RequestFollowFollower
import net.pantasystem.milktea.api.misskey.v11.MisskeyAPIV11
import net.pantasystem.milktea.app_store.user.FollowFollowerPagingStore
import net.pantasystem.milktea.app_store.user.RequestType
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.toUser
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton


class FollowFollowerPagingStoreImpl(
    override val type: RequestType,
    val userDataSource: UserDataSource,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val mastodonAPIProvider: MastodonAPIProvider,
    val getAccount: GetAccount,
    val loggerFactory: Logger.Factory,
    val noteDataSourceAdder: NoteDataSourceAdder,
) : FollowFollowerPagingStore {

    class Factory @Inject constructor(
        val userDataSource: UserDataSource,
        val misskeyAPIProvider: MisskeyAPIProvider,
        val mastodonAPIProvider: MastodonAPIProvider,
        val loggerFactory: Logger.Factory,
        val getAccount: GetAccount,
        val noteDataSourceAdder: NoteDataSourceAdder
    ) : FollowFollowerPagingStore.Factory {
        override fun create(type: RequestType): FollowFollowerPagingStore {
            return FollowFollowerPagingStoreImpl(
                type,
                misskeyAPIProvider = misskeyAPIProvider,
                loggerFactory = loggerFactory,
                getAccount = getAccount,
                userDataSource = userDataSource,
                noteDataSourceAdder = noteDataSourceAdder,
                mastodonAPIProvider = mastodonAPIProvider,
            )
        }
    }

    private val loader = FollowFollowerPagingModelImpl(
        requestType = type,
        getAccount = getAccount,
        userDataSource = userDataSource,
        misskeyAPIProvider = misskeyAPIProvider,
        mastodonAPIProvider = mastodonAPIProvider,
    )

    private val previousPagingController = PreviousPagingController(
        loader,
        loader,
        loader,
        loader,
    )


    private val _state =
        MutableStateFlow<PageableState<List<User.Id>>>(PageableState.Loading.Init())
    override val state: Flow<PageableState<List<User.Id>>>
        get() = loader.state.map { state ->
            state.convert { list ->
                list.map {
                    it.userId
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val users: Flow<List<User.Detail>>
        get() = state.map {
            it.content
        }.filter {
            it is StateContent.Exist
        }.flatMapLatest { stateContent ->
            val ids = (stateContent as StateContent.Exist).rawContent
            val accountId = ids.map { it.accountId }.distinct().first()
            userDataSource.observeIn(accountId, ids.map { it.id }).map { list ->
                list.mapNotNull { user ->
                    user as User.Detail?
                }
            }.map {
                val userMap = it.associateBy { it.id }
                ids.mapNotNull { userId ->
                    userMap[userId]
                }
            }
        }.catch {
            loggerFactory.create("FollowFollowerPagingModel").error("error", it)
        }


    override suspend fun loadPrevious() {
        previousPagingController.loadPrevious().onFailure {
            loggerFactory.create("FollowFollowerPagingModel").error("フォロー・フォロワーの読み込みに失敗", it)
        }
    }

    override suspend fun clear() {
        loader.mutex.withLock {
            loader.setState(PageableState.Loading.Init())
        }
    }
}

internal interface Paginator {
    companion object

    val idHolder: IdHolder
    suspend fun next(): List<UserDTO>
    suspend fun init()
}


@Singleton
internal class PaginatorFactory @Inject constructor(
    val misskeyAPIProvider: MisskeyAPIProvider,
    val getAccount: GetAccount,

    val logger: Logger.Factory,
) {
    suspend fun create(type: RequestType, idHolder: IdHolder): Paginator {
        val account = getAccount.get(type.userId.accountId)
        val api = misskeyAPIProvider.get(account)
        return if (api is MisskeyAPIV10) {
            V10Paginator(
                account,
                api,
                type,
                idHolder,
            )
        } else {
            DefaultPaginator(
                account,
                api as MisskeyAPIV11,
                type,
                logger.create("DefaultPaginator"),
                idHolder,
            )
        }

    }
}


internal class IdHolder(
    var nextId: String? = null
)

@Suppress("BlockingMethodInNonBlockingContext")
internal class DefaultPaginator(
    val account: Account,
    private val misskeyAPI: MisskeyAPIV11,
    val type: RequestType,
    private val logger: Logger?,
    override val idHolder: IdHolder,

    ) : Paginator {


    private val api =
        if (type is RequestType.Follower) misskeyAPI::followers else misskeyAPI::following

    override suspend fun next(): List<UserDTO> {
        logger?.debug("next: ${idHolder.nextId}")
        val res = api.invoke(
            RequestUser(
                account.token,
                userId = type.userId.id,
                untilId = idHolder.nextId
            )
        ).body()
            ?: return emptyList()
        idHolder.nextId = res.last().id
        require(idHolder.nextId != null)
        return res.mapNotNull {
            it.followee ?: it.follower
        }
    }

    override suspend fun init() {
        idHolder.nextId = null
    }
}


@Suppress("BlockingMethodInNonBlockingContext")
internal class V10Paginator(
    val account: Account,
    private val misskeyAPIV10: MisskeyAPIV10,
    val type: RequestType,
    override val idHolder: IdHolder,
) : Paginator {
    private val api =
        if (type is RequestType.Follower) misskeyAPIV10::followers else misskeyAPIV10::following

    override suspend fun next(): List<UserDTO> {
        val res = api.invoke(
            RequestFollowFollower(
                i = account.token,
                cursor = idHolder.nextId,
                userId = type.userId.id
            )
        ).body() ?: return emptyList()
        idHolder.nextId = res.next
        return res.users
    }

    override suspend fun init() {
        idHolder.nextId = null
    }
}

class FollowFollowerPagingModelImpl(
    val requestType: RequestType,
    val getAccount: GetAccount,
    val userDataSource: UserDataSource,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val mastodonAPIProvider: MastodonAPIProvider,
) : StateLocker,
    PreviousLoader<FollowFollowerResponseItemType>,
    EntityConverter<FollowFollowerResponseItemType, UserIdAndNextId>,
    PaginationState<UserIdAndNextId>,
    IdGetter<String> {
    override val mutex: Mutex = Mutex()

    private val _state =
        MutableStateFlow<PageableState<List<UserIdAndNextId>>>(PageableState.Loading.Init())
    override val state: Flow<PageableState<List<UserIdAndNextId>>> = _state
    override suspend fun convertAll(list: List<FollowFollowerResponseItemType>): List<UserIdAndNextId> {
        val account = getAccount.get(requestType.userId.accountId)
        val users = list.map {
            when (it) {
                is FollowFollowerResponseItemType.Default -> {
                    it.userDTO.toUser(account, true)
                }
                is FollowFollowerResponseItemType.Mastodon -> {
                    it.userDTO.toModel(account)
                }
                is FollowFollowerResponseItemType.V10 -> {
                    it.userDTO.toUser(account, true)
                }
            }
        }
        userDataSource.addAll(users)
        return list.map {
            it.toUserIdAndNextId(account.accountId)
        }
    }

    override suspend fun loadPrevious(): Result<List<FollowFollowerResponseItemType>> =
        runCancellableCatching {
            val account = getAccount.get(requestType.userId.accountId)
            when (account.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    when (misskeyAPIProvider.get(account)) {
                        is MisskeyAPIV11 -> {
                            DefaultLoader(
                                requestType,
                                account,
                                misskeyAPIProvider,
                                this@FollowFollowerPagingModelImpl
                            )
                        }
                        is MisskeyAPIV10 -> {
                            V10Loader(
                                requestType,
                                account,
                                misskeyAPIProvider,
                                this@FollowFollowerPagingModelImpl
                            )
                        }
                        else -> throw IllegalStateException("not support follow follower list")
                    }
                }
                Account.InstanceType.MASTODON -> {
                    MastodonLoader(
                        requestType,
                        account,
                        mastodonAPIProvider = mastodonAPIProvider,
                        this@FollowFollowerPagingModelImpl,
                        this@FollowFollowerPagingModelImpl
                    )
                }
            }.loadPrevious().getOrThrow()
        }

    override fun getState(): PageableState<List<UserIdAndNextId>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<UserIdAndNextId>>) {
        _state.value = state
    }

    override suspend fun getSinceId(): String? {
        return null
    }

    override suspend fun getUntilId(): String? {
        return (_state.value.content as? StateContent.Exist)?.rawContent?.lastOrNull()?.nextId
    }

}

class V10Loader(
    val type: RequestType,
    val account: Account,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val idGetter: IdGetter<String>,
) : PreviousLoader<FollowFollowerResponseItemType> {
    override suspend fun loadPrevious(): Result<List<FollowFollowerResponseItemType>> {
        val api = misskeyAPIProvider.get(account) as MisskeyAPIV10
        TODO("Not yet implemented")
    }
}

class DefaultLoader(
    val type: RequestType,
    val account: Account,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val idGetter: IdGetter<String>,
) : PreviousLoader<FollowFollowerResponseItemType> {
    override suspend fun loadPrevious(): Result<List<FollowFollowerResponseItemType>> {
        val api = misskeyAPIProvider.get(account) as MisskeyAPIV11

        TODO("Not yet implemented")
    }
}

class MastodonLoader(
    val type: RequestType,
    val account: Account,
    val mastodonAPIProvider: MastodonAPIProvider,
    val idGetter: IdGetter<String>,
    val state: PaginationState<UserIdAndNextId>,
) : PreviousLoader<FollowFollowerResponseItemType> {
    override suspend fun loadPrevious(): Result<List<FollowFollowerResponseItemType>> =
        runCancellableCatching {
            val isEmpty = (state.getState().content as? StateContent.Exist?)?.rawContent.isNullOrEmpty()
            if (isEmpty && idGetter.getUntilId() == null) {
                return@runCancellableCatching emptyList()
            }
            val api = mastodonAPIProvider.get(account)
            val response = when (type) {
                is RequestType.Follower -> {
                    api.getFollowers(
                        type.userId.id,
                        maxId = idGetter.getUntilId()
                    )
                }
                is RequestType.Following -> {
                    api.getFollowing(
                        type.userId.id,
                        maxId = idGetter.getUntilId()
                    )
                }
            }.throwIfHasError()
            val linkHeader = response.headers()["link"]
            val body = requireNotNull(response.body())
            val ids = body.map {
                it.id
            }
            val idAndRelation = requireNotNull(
                mastodonAPIProvider.get(account).getAccountRelationships(ids)
                    .throwIfHasError().body()
            ).associateBy {
                it.id
            }
            val nextId = MastodonLinkHeaderDecoder(linkHeader).getMaxId()
            body.map {
                FollowFollowerResponseItemType.Mastodon(
                    userDTO = it,
                    nextId = nextId,
                    relationship = idAndRelation[it.id]
                )
            }

        }
}

data class UserIdAndNextId(
    val userId: User.Id,
    val nextId: String?,
)

fun FollowFollowerResponseItemType.toUserIdAndNextId(accountId: Long): UserIdAndNextId {
    return UserIdAndNextId(
        userId = when (this) {
            is FollowFollowerResponseItemType.Default -> User.Id(accountId, userDTO.id)
            is FollowFollowerResponseItemType.Mastodon -> User.Id(accountId, userDTO.id)
            is FollowFollowerResponseItemType.V10 -> User.Id(accountId, userDTO.id)
        },
        nextId = nextId
    )
}

sealed interface FollowFollowerResponseItemType {
    val nextId: String?

    data class Default(val userDTO: UserDTO, override val nextId: String?) :
        FollowFollowerResponseItemType

    data class V10(val userDTO: UserDTO, override val nextId: String?) :
        FollowFollowerResponseItemType

    data class Mastodon(
        val userDTO: MastodonAccountDTO,
        val relationship: MastodonAccountRelationshipDTO?,
        override val nextId: String?
    ) :
        FollowFollowerResponseItemType
}