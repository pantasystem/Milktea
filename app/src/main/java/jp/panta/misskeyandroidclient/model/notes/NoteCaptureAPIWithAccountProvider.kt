package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.streaming.SocketWithAccountProvider
import jp.panta.misskeyandroidclient.streaming.notes.NoteCaptureAPI
import jp.panta.misskeyandroidclient.streaming.notes.NoteCaptureAPIImpl
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * NoteCaptureAPIのインスタンスをAccountに基づきいい感じに取得や生成をできるようにする。
 */
class NoteCaptureAPIWithAccountProvider(
    private val socketWithAccountProvider: SocketWithAccountProvider,
    private val loggerFactory: Logger.Factory? = null
) {

    private val accountIdWithNoteCaptureAPI = mutableMapOf<Long, NoteCaptureAPI>()
    private val lock = Mutex()

    fun get(account: Account) : NoteCaptureAPI = runBlocking{
        lock.withLock {
            var channelAPI = accountIdWithNoteCaptureAPI[account.accountId]
            if(channelAPI != null) {
                return@runBlocking channelAPI
            }

            val socket = socketWithAccountProvider.get(account)
            channelAPI = NoteCaptureAPIImpl(socket, loggerFactory)
            accountIdWithNoteCaptureAPI[account.accountId] = channelAPI

            return@runBlocking channelAPI
        }

    }

}