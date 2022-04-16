package net.pantasystem.milktea.data.streaming

import net.pantasystem.milktea.data.model.account.Account

interface SocketWithAccountProvider {

    fun get(account: Account) : Socket

    fun get(accountId: Long) : Socket?

    fun all(): List<Socket>
}