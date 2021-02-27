package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.model.account.Account

interface SocketWithAccountProvider {

    fun get(account: Account) : Socket
}