package jp.panta.misskeyandroidclient.streaming.channel

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.streaming.SocketWithAccountProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChannelAPIWithAccountProvider(
    val socketWithAccountProvider: SocketWithAccountProvider,
    private val loggerFactory: Logger.Factory
) {

    private val accountWithChannelAPI = mutableMapOf<Long, ChannelAPI>()
    private val logger = loggerFactory.create("ChannelAPIWithAccountProvider")

    fun get(account: Account) : ChannelAPI{
        synchronized(accountWithChannelAPI) {
            logger.debug("ChannelAPIWithAccountProvider get accountId=${account.accountId} hash=${hashCode()}")
            var channelAPI = accountWithChannelAPI[account.accountId]
            if(channelAPI != null){
                return channelAPI
            }
            channelAPI = ChannelAPI(socketWithAccountProvider.get(account), loggerFactory)
            accountWithChannelAPI[account.accountId] = channelAPI
            require(accountWithChannelAPI.put(account.accountId, channelAPI) == null)
            return channelAPI
        }
    }
}