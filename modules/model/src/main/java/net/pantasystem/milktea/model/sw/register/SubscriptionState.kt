package net.pantasystem.milktea.model.sw.register

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionState(
    @SerialName("state") val state: String,
    @SerialName("key") val key: String? = null,
)