package net.pantasystem.milktea.api.misskey.v12.user.reaction

@kotlinx.serialization.Serializable
data class UserReactionRequest(
    val i: String,
    val untilId: String? = null,
    val limit: Int,
    val userId: String
)
