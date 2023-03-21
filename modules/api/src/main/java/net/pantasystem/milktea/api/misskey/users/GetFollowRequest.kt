package net.pantasystem.milktea.api.misskey.users

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class GetFollowRequest(
    @SerialName("i")
    val i: String,

    @SerialName("sinceId")
    val sinceId: String? = null,

    @SerialName("untilId")
    val untilId: String? = null,

    @SerialName("limit")
    val limit: Int? = null,
)