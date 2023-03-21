package net.pantasystem.milktea.api.misskey.clip

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.misskey.users.UserDTO

@kotlinx.serialization.Serializable
data class ClipDTO(
    @SerialName("id")
    val id: String,

    @SerialName("createdAt")
    val createdAt: Instant,

    @SerialName("userId")
    val userId: String,

    @SerialName("user")
    val user: UserDTO,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("isPublic")
    val isPublic: Boolean
)