package net.pantasystem.milktea.api.misskey.users.follow

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UpdateUserFollowRequest(
    @SerialName("i") val i: String,
    @SerialName("userId") val userId: String,
    @SerialName("notify") val notify: Boolean? = null,
    @SerialName("withReplies") val withReplies: Boolean? = null,
)