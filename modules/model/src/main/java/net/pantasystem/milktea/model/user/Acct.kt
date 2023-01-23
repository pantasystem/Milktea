package net.pantasystem.milktea.model.user

data class Acct(val userName: String, val host: String?)


fun Acct(acct: String): Acct {
    val userNameAndHost = acct.split("@").filter { it.isNotBlank() }
    val userName = userNameAndHost[0]
    val host = userNameAndHost.getOrNull(1)
    return Acct(userName, host)
}