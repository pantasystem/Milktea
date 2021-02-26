package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.streaming.network.Socket

class TestSocketWithAccountProviderImpl : SocketWithAccountProvider{

    override fun get(account: Account): Socket {
        return TestSocketImpl()
    }
}