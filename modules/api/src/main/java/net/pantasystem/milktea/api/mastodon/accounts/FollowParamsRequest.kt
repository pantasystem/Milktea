package net.pantasystem.milktea.api.mastodon.accounts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowParamsRequest(
    @SerialName("reblogs") val reblogs: Boolean? = null,
    @SerialName("notify") val notify: Boolean? = null,
)