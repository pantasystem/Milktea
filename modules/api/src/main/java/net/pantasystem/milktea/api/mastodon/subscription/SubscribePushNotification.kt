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
        @SerialName("alerts") val alerts: Alerts,
        @SerialName("policy") val policy: String? = null,
    ) {
        @Serializable
        data class Alerts(
            @SerialName("mention") val mention: Boolean? = null,
            @SerialName("status") val status: Boolean? = null,
            @SerialName("reblog") val reblog: Boolean? = null,
            @SerialName("follow") val follow: Boolean? = null,
            @SerialName("follow_request") val followRequest: Boolean? = null,
            @SerialName("favourite") val favourite: Boolean? = null,
            @SerialName("poll") val poll: Boolean? = null,
            @SerialName("update") val update: Boolean? = null,
        )
    }

}