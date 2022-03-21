package jp.panta.misskeyandroidclient.model.channel

import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.misskey.throwIfHasError
import jp.panta.misskeyandroidclient.api.misskey.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.api.misskey.v12.channel.ChannelDTO
import jp.panta.misskeyandroidclient.api.misskey.v12.channel.FindPageable
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.util.PageableState
import jp.panta.misskeyandroidclient.util.StateContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import retrofit2.Response

enum class ChannelListType {
    OWNED, FOLLOWED, FEATURED
}

class ChannelPagingModel(
    val account: Account,
    val type: ChannelListType,
    val encryption: Encryption,
    private val channelStateModel: ChannelStateModel,
    val misskeyAPIProvider: MisskeyAPIProvider,
) : EntityConverter<ChannelDTO, Channel.Id>, PreviousLoader<ChannelDTO>, FutureLoader<ChannelDTO>,
    IdGetter<Channel.Id>, StateLocker, PaginationState<Channel.Id> {

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
        if (type == ChannelListType.FEATURED) {
            return null
        }
        return (getState().content as? StateContent.Exist)?.rawContent?.firstOrNull()
    }

    override suspend fun getUntilId(): Channel.Id? {
        if (type == ChannelListType.FEATURED) {
            return null
        }
        return (getState().content as? StateContent.Exist)?.rawContent?.lastOrNull()
    }

    override fun setState(state: PageableState<List<Channel.Id>>) {
        _state.value = state
    }

    override suspend fun convertAll(list: List<ChannelDTO>): List<Channel.Id> {
        return channelStateModel.addAll(list.map { it.toModel(account) }).map { it.id }
    }

    override suspend fun loadFuture(): Response<List<ChannelDTO>> {
        val api = (misskeyAPIProvider.get(account) as MisskeyAPIV12)
        val i = account.getI(encryption)
        val res = when (type) {
            ChannelListType.FOLLOWED -> api.followedChannels(
                FindPageable(
                    i = i,
                    sinceId = null,
                    untilId = getSinceId()?.channelId
                )
            )
            ChannelListType.OWNED -> api.ownedChannels(
                FindPageable(
                    i = i,
                    sinceId = null,
                    untilId = getSinceId()?.channelId
                )
            )
            ChannelListType.FEATURED -> {
                throw IllegalStateException("featuredはサポートしていません。")
            }
        }
        return res.throwIfHasError()
    }

    override suspend fun loadPrevious(): Response<List<ChannelDTO>> {
        val api = (misskeyAPIProvider.get(account) as MisskeyAPIV12)
        val i = account.getI(encryption)
        val res = when (type) {
            ChannelListType.FOLLOWED -> api.followedChannels(
                FindPageable(
                    i = i,
                    sinceId = null,
                    untilId = getUntilId()?.channelId
                )
            )
            ChannelListType.OWNED -> api.ownedChannels(
                FindPageable(
                    i = i,
                    sinceId = null,
                    untilId = getUntilId()?.channelId
                )
            )
            ChannelListType.FEATURED -> {
                api.featuredChannels(I(i))
            }
        }
        return res.throwIfHasError()
    }


}