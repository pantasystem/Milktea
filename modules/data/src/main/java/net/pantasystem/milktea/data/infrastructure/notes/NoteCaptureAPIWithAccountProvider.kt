package net.pantasystem.milktea.data.infrastructure.notes

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api_streaming.NoteCaptureAPI
import net.pantasystem.milktea.api_streaming.NoteCaptureAPIImpl
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider
import net.pantasystem.milktea.model.account.Account
import javax.inject.Inject

interface NoteCaptureAPIWithAccountProvider {
    fun get(account: Account): NoteCaptureAPI?
}
/**
 * NoteCaptureAPIのインスタンスをAccountに基づきいい感じに取得や生成をできるようにする。
 */
class NoteCaptureAPIWithAccountProviderImpl @Inject constructor(
    private val socketWithAccountProvider: SocketWithAccountProvider,
    private val loggerFactory: Logger.Factory?
) : NoteCaptureAPIWithAccountProvider {

    private val accountIdWithNoteCaptureAPI = mutableMapOf<Long, NoteCaptureAPI>()
    private val lock = Mutex()

    override fun get(account: Account) : NoteCaptureAPI? = runBlocking{
        lock.withLock {
            var channelAPI = accountIdWithNoteCaptureAPI[account.accountId]
            if(channelAPI != null) {
                return@runBlocking channelAPI
            }

            val socket = socketWithAccountProvider.get(account)
                ?: return@runBlocking null
            channelAPI =
                NoteCaptureAPIImpl(socket, loggerFactory)
            accountIdWithNoteCaptureAPI[account.accountId] = channelAPI

            return@runBlocking channelAPI
        }

    }

}