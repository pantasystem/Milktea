package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.model.account.Account

class TestSocketWithAccountProviderImpl : SocketWithAccountProvider{

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