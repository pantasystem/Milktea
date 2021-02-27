package jp.panta.misskeyandroidclient.streaming.channel

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.streaming.SocketWithAccountProvider

class ChannelAPIWithAccountProvider(
    val socketWithAccountProvider: SocketWithAccountProvider
) {

    private val accountWithChannelAPI = mutableMapOf<Long, ChannelAPI>()


    fun get(account: Account) : ChannelAPI{
        synchronized(accountWithChannelAPI) {
            var channelAPI = accountWithChannelAPI[account.accountId]
            if(channelAPI != null){
                return channelAPI
            }
            channelAPI = ChannelAPI(socketWithAccountProvider.get(account))
            accountWithChannelAPI[account.accountId] = channelAPI
            return channelAPI
        }
    }
}