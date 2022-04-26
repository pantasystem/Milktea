package net.pantasystem.milktea.model.account

interface Auth {
    suspend fun check(): Boolean
    suspend fun getToken(): String?
    suspend fun getCurrentAccount(): Account?
}

interface AuthById {
    suspend fun check(id: Long): Boolean
    suspend fun getToken(id: Long): String?
}

fun interface GetAccount {
    suspend fun get(id: Long): Account
}