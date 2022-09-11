package net.pantasystem.milktea.api.misskey.groups

import kotlinx.serialization.Serializable

@Serializable
data class InvitationDTO(
    val id: String,
    val group: GroupDTO
)