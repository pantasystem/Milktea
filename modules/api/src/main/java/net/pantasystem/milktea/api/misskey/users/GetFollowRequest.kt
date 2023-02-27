package net.pantasystem.milktea.api.misskey.users

@kotlinx.serialization.Serializable
data class GetFollowRequest(
    val i: String,
    val sinceId: String? = null,
    val untilId: String? = null,
    val limit: Int? = null,
)