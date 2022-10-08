package net.pantasystem.milktea.model.user

data class Acct(val acct: String) {
    private val userNameAndHost = acct.split("@").filter { it.isNotBlank() }
    val userName = userNameAndHost[0]
    val host = userNameAndHost.getOrNull(1)
}
