package net.pantasystem.milktea.api.misskey.users.renote.mute

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class DeleteRenoteMuteRequest(
    @SerialName("i") val i: String,
    @SerialName("userId") val userId: String
)