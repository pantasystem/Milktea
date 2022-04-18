package net.pantasystem.milktea.model.hashtag

import kotlinx.serialization.Serializable

@Serializable
data class HashTag(
    val tag: String,
    val mentionedUserCount: Int?,
    val mentionedLocalUserCount: Int?,
    val mentionedRemoteUserCount: Int?,
    val attachedUsersCount: Int?,
    val attachedLocalUsersCount: Int?,
    val attachedRemoteUsersCount: Int?
)