package net.pantasystem.milktea.api.mastodon.subscription

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WebPushSubscriptionAlerts(
    @SerialName("mention") val mention: Boolean? = null,
    @SerialName("status") val status: Boolean? = null,
    @SerialName("reblog") val reblog: Boolean? = null,
    @SerialName("follow") val follow: Boolean? = null,
    @SerialName("follow_request") val followRequest: Boolean? = null,
    @SerialName("favourite") val favourite: Boolean? = null,
    @SerialName("poll") val poll: Boolean? = null,
    @SerialName("update") val update: Boolean? = null,
)