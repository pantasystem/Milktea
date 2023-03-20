package net.pantasystem.milktea.api.misskey.v12.user.reaction

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class UserReactionRequest(
    @SerialName("i")
    val i: String,

    @SerialName("untilId")
    val untilId: String? = null,

    @SerialName("limit")
    val limit: Int,

    @SerialName("userId")
    val userId: String
)
