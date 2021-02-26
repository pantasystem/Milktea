package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.streaming.network.Socket

interface SocketWithAccountProvider {

    fun get(account: Account) : Socket
}