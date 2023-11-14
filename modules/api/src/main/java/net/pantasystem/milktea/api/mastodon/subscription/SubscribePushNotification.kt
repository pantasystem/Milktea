package net.pantasystem.milktea.api.mastodon.subscription

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscribePushNotification(
    @SerialName("subscription") val subscription: Subscription,
    @SerialName("data") val data: Data,
) {
    @Serializable
    data class Subscription(
        @SerialName("endpoint") val endpoint: String,
        @SerialName("keys") val keys: Keys,

        ) {
        @Serializable
        data class Keys(
            @SerialName("p256dh") val p256dh: String,
            @SerialName("auth") val auth: String,
        )
    }

    @Serializable
    data class Data(
        @SerialName("alerts") val alerts: WebPushSubscriptionAlerts,
        @SerialName("policy") val policy: String? = null,
    )

}