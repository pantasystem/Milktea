package net.pantasystem.milktea.api.misskey.clip

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class FindUsersClipRequest(
    @SerialName("i")
    val i: String,

    @SerialName("userId")
    val userId: String,

    @SerialName("limit")
    val limit: Int,

    @SerialName("untilId")
    val untilId: String? = null,

    @SerialName("sinceId")
    val sinceId: String? = null,
)