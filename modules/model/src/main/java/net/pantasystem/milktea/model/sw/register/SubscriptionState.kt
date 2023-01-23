package net.pantasystem.milktea.model.sw.register

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionState(
    val state: String,
    val key: String
)