package net.pantasystem.milktea.api.misskey.users

@kotlinx.serialization.Serializable
data class CreateMuteUserRequest(
    val i: String,
    val userId: String,
    val expiresAt: Long? = null,
)
