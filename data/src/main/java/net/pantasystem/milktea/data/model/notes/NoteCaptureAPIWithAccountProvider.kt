package net.pantasystem.milktea.data.model.notes

import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider
import net.pantasystem.milktea.data.streaming.notes.NoteCaptureAPI
import net.pantasystem.milktea.data.streaming.notes.NoteCaptureAPIImpl
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.Logger
import javax.inject.Inject

interface NoteCaptureAPIWithAccountProvider {
    fun get(account: Account): NoteCaptureAPI
}
/**
 * NoteCaptureAPIのインスタンスをAccountに基づきいい感じに取得や生成をできるようにする。
 */
class NoteCaptureAPIWithAccountProviderImpl @Inject constructor(
    private val socketWithAccountProvider: SocketWithAccountProvider,
    private val loggerFactory: Logger.Factory?
) : NoteCaptureAPIWithAccountProvider{

    private val accountIdWithNoteCaptureAPI = mutableMapOf<Long, NoteCaptureAPI>()
    private val lock = Mutex()

    override fun get(account: Account) : NoteCaptureAPI = runBlocking{
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