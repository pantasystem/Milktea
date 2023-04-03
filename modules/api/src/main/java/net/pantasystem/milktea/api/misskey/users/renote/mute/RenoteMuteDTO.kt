package net.pantasystem.milktea.api.misskey.users.renote.mute

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.misskey.users.UserDTO

@kotlinx.serialization.Serializable
data class RenoteMuteDTO(
    @SerialName("id") val id: String,
    @SerialName("createdAt") val createdAt: Instant,
    @SerialName("mutee") val mutee: UserDTO,
    @SerialName("muteeId") val muteeId: String,
)

@kotlinx.serialization.Serializable
data class RenoteMutesRequest(
    @SerialName("i") val i: String,
    @SerialName("sinceId") val sinceId: String? = null,
    @SerialName("untilId") val untilId: String? = null,
)