package net.pantasystem.milktea.data.infrastructure.channel

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.misskey.I
import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12
import net.pantasystem.milktea.api.misskey.v12.channel.ChannelDTO
import net.pantasystem.milktea.api.misskey.v12.channel.FindPageable
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider

import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.channel.ChannelStateModel

enum class ChannelListType {
    OWNED, FOLLOWED, FEATURED
}

class ChannelPagingModel @AssistedInject constructor(
    val encryption: Encryption,
    private val channelStateModel: ChannelStateModel,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val accountRepository: AccountRepository,
    val loggerFactory: Logger.Factory,
    @Assisted val accountId: Long,
    @Assisted val type: ChannelListType,
) : EntityConverter<ChannelDTO, Channel.Id>, PreviousLoader<ChannelDTO>,
    IdGetter<Channel.Id>, StateLocker, PaginationState<Channel.Id> {

    val logger: Logger by lazy {
        loggerFactory.create("ChannelPagingModel")
    }

    @AssistedFactory
    interface ModelAssistedFactory {
        fun create(accountId: Long, type: ChannelListType): ChannelPagingModel
    }

    override val mutex: Mutex = Mutex()


    private val _state = MutableStateFlow<PageableState<List<Channel.Id>>>(
        PageableState.Fixed(
            StateContent.NotExist()
        )
    )
    override val state: Flow<PageableState<List<Channel.Id>>>
        get() = _state


    override fun getState(): PageableState<List<Channel.Id>> {
        return _state.value
    }

    override suspend fun getSinceId(): Channel.Id? {
        return (getState().content as? StateContent.Exist)?.rawContent?.firstOrNull()
    }

    override suspend fun getUntilId(): Channel.Id? {
        return (getState().content as? StateContent.Exist)?.rawContent?.lastOrNull()
    }

    override fun setState(state: PageableState<List<Channel.Id>>) {
        logger.debug("setState:$state")
        _state.value = state
    }

    override suspend fun convertAll(list: List<ChannelDTO>): List<Channel.Id> {
        val account = accountRepository.get(accountId).getOrThrow()
        return channelStateModel.addAll(list.map { it.toModel(account) }).map { it.id }
    }
//    NOTE: MisskeyのAPIがバグってるのか正常に動かない（Postmanからもチェック済み）
//    override suspend fun loadFuture(): Response<List<ChannelDTO>> {
//        val sinceId = getSinceId()?.channelId
//        logger.debug("loadFuture type:$type, sinceId:$sinceId")
//        val account = accountRepository.get(accountId).getOrThrow()
//        val api = (misskeyAPIProvider.get(account) as MisskeyAPIV12)
//        val i = account.token
//        val res = when (type) {
//            ChannelListType.FOLLOWED -> api.followedChannels(
//                FindPageable(
//                    i = i,
//                    sinceId = sinceId,
//                    untilId = null,
//                )
//            )
//            ChannelListType.OWNED -> api.ownedChannels(
//                FindPageable(
//                    i = i,
//                    sinceId = sinceId,
//                    untilId = null
//                )
//            )
//            ChannelListType.FEATURED -> {
//                throw IllegalStateException("featuredはサポートしていません。")
//            }
//        }
//        return res.throwIfHasError()
//    }

    override suspend fun loadPrevious(): Result<List<ChannelDTO>> {
        val account = accountRepository.get(accountId).getOrThrow()
        val api = (misskeyAPIProvider.get(account) as MisskeyAPIV12)
        val i = account.token
        val res = when (type) {
            ChannelListType.FOLLOWED -> {
                logger.debug("loadPrevious:${_state.value}")
                if (getUntilId() != null) {
                    throw IllegalStateException()
                }
                api.followedChannels(
                    FindPageable(
                        i = i,
                        sinceId = null,
                        untilId = null,
//                    untilId = getUntilId()?.channelId,
                        limit = 99,
                    )
                )
            }
            ChannelListType.OWNED -> {
                if (getUntilId() != null) {
                    // TODO: APIのページネーションが修正されたら修正する
                    throw IllegalStateException()
                }
                api.ownedChannels(
                    FindPageable(
                        i = i,
                        sinceId = null,
                        untilId = null,
//                    untilId = getUntilId()?.channelId,
                        limit = 99,
                    )
                )
            }
            ChannelListType.FEATURED -> {
                if (getUntilId() != null) {
                    // NOTE: featuredはページネーションできないので
                    throw IllegalStateException()
                }
                api.featuredChannels(I(i))
            }
        }
        logger.debug("loadPrevious res:${res.code()}")
        return runCancellableCatching {
            res.throwIfHasError().body()!!
        }
    }


    suspend fun clear() {
        mutex.withLock {
            _state.value = PageableState.Loading.Init()
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeChannels(): Flow<PageableState<List<Channel>>> {
        return channelStateModel.state.flatMapLatest { globalState ->
            state.map { state ->
                state.convert { list ->
                    list.mapNotNull { id ->
                        globalState.get(id)
                    }
                }
            }
        }.distinctUntilChanged()
    }
}