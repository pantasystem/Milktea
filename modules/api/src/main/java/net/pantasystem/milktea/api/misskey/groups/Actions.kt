package net.pantasystem.milktea.api.misskey.groups

import kotlinx.serialization.Serializable

@Serializable
data class DeleteGroupDTO(
    val i: String,
    val groupId: String
)

@Serializable
data class CreateGroupDTO(
    val i: String,
    val name: String
)

@Serializable
data class AcceptInvitationDTO(
    val i: String,
    val invitationId: String
)

@Serializable
data class RejectInvitationDTO(
    val i: String,
    val invitationId: String
)

@Serializable
data class InviteUserDTO(
    val i: String,
    val userId: String,
    val groupId: String,
)

@Serializable
data class RemoveUserDTO(
    val i: String,
    val userId: String,
    val groupId: String
)

@Serializable
data class ShowGroupDTO(
    val i: String,
    val groupId: String
)

@Serializable
data class TransferGroupDTO(
    val i: String,
    val groupId: String,
    val userId: String
)

@Serializable
data class UpdateGroupDTO(
    val i: String,
    val groupId: String,
    val name: String
)