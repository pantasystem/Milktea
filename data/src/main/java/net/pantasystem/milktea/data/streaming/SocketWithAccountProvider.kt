package net.pantasystem.milktea.data.streaming

import net.pantasystem.milktea.model.account.Account

interface SocketWithAccountProvider {

    fun get(account: net.pantasystem.milktea.model.account.Account) : Socket

    fun get(accountId: Long) : Socket?

    fun all(): List<Socket>
}