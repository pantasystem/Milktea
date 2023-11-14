package net.pantasystem.milktea.model.sw.register

interface SubscriptionRegistration {
    suspend fun registerAll() : Int
    suspend fun register(accountId: Long): Result<Unit>
}