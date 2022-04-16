package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.model.account.Account
import net.pantasystem.milktea.data.streaming.Socket
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider

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