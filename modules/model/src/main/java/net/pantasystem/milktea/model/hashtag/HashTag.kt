package net.pantasystem.milktea.model.hashtag

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HashTag(
    @SerialName("tag") val tag: String,
    @SerialName("mentionedUserCount") val mentionedUserCount: Int?,
    @SerialName("mentionedLocalUserCount") val mentionedLocalUserCount: Int?,
    @SerialName("mentionedRemoteUserCount") val mentionedRemoteUserCount: Int?,
    @SerialName("attachedUsersCount") val attachedUsersCount: Int?,
    @SerialName("attachedLocalUsersCount") val attachedLocalUsersCount: Int?,
    @SerialName("attachedRemoteUsersCount") val attachedRemoteUsersCount: Int?
)