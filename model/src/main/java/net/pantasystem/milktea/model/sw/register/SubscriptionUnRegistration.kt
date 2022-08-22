package net.pantasystem.milktea.model.sw.register

interface SubscriptionUnRegistration {
    suspend fun unregister(accountId: Long)
}