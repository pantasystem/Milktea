package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.streaming.SocketWithAccountProvider
import jp.panta.misskeyandroidclient.streaming.notes.NoteCaptureAPI

/**
 * NoteCaptureAPIのインスタンスをAccountに基づきいい感じに取得や生成をできるようにする。
 */
class NoteCaptureAPIWithAccountProvider(
    private val socketWithAccountProvider: SocketWithAccountProvider,
    private val loggerFactory: Logger.Factory? = null
) {

    private val accountIdWithNoteCaptureAPI = mutableMapOf<Long, NoteCaptureAPI>()

    fun get(account: Account) : NoteCaptureAPI {
        synchronized(accountIdWithNoteCaptureAPI) {
            var channelAPI = accountIdWithNoteCaptureAPI[account.accountId]
            if(channelAPI != null) {
                return channelAPI
            }

            val socket = socketWithAccountProvider.get(account)
            channelAPI = NoteCaptureAPI(socket, loggerFactory)
            accountIdWithNoteCaptureAPI[account.accountId] = channelAPI

            return channelAPI
        }
    }

}