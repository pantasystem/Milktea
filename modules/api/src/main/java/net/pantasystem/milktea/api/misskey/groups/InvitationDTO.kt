package net.pantasystem.milktea.api.misskey.groups

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InvitationDTO(
    @SerialName("id")
    val id: String,

    @SerialName("group")
    val group: GroupDTO
)