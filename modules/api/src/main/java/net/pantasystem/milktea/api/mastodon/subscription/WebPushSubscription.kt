package net.pantasystem.milktea.api.mastodon.subscription

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WebPushSubscription(
    @SerialName("id") val id: String,
    @SerialName("endpoint") val endpoint: String,
    @SerialName("alerts") val alerts: WebPushSubscriptionAlerts,
    @SerialName("server_key") val serverKey: String,
)