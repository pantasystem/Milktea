package jp.panta.misskeyandroidclient.streaming

import net.pantasystem.milktea.api_streaming.Socket
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider
import net.pantasystem.milktea.model.account.Account

class TestSocketWithAccountProviderImpl : SocketWithAccountProvider {

    override fun get(account: Account): Socket {
        return TestSocketImpl()
    }

    override fun get(accountId: Long): Socket {
        return TestSocketImpl()
    }

    override fun all(): List<Socket> {
        return listOf(TestSocketImpl())
    }
}