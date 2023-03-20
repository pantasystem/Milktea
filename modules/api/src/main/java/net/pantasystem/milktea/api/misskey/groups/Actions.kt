package net.pantasystem.milktea.api.misskey.groups

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteGroupDTO(
    @SerialName("i")
    val i: String,

    @SerialName("groupId")
    val groupId: String
)

@Serializable
data class CreateGroupDTO(
    @SerialName("i")
    val i: String,

    @SerialName("name")
    val name: String
)

@Serializable
data class AcceptInvitationDTO(
    @SerialName("i")
    val i: String,

    @SerialName("invitationId")
    val invitationId: String
)

@Serializable
data class RejectInvitationDTO(
    @SerialName("i")
    val i: String,

    @SerialName("invitationId")
    val invitationId: String
)

@Serializable
data class InviteUserDTO(
    @SerialName("i")
    val i: String,

    @SerialName("userId")
    val userId: String,

    @SerialName("groupId")
    val groupId: String,
)

@Serializable
data class RemoveUserDTO(
    @SerialName("i")
    val i: String,

    @SerialName("userId")
    val userId: String,

    @SerialName("groupId")
    val groupId: String
)

@Serializable
data class ShowGroupDTO(
    @SerialName("i")
    val i: String,

    @SerialName("groupId")
    val groupId: String
)

@Serializable
data class TransferGroupDTO(
    @SerialName("i")
    val i: String,

    @SerialName("groupId")
    val groupId: String,

    @SerialName("userId")
    val userId: String
)

@Serializable
data class UpdateGroupDTO(
    @SerialName("i")
    val i: String,

    @SerialName("groupId")
    val groupId: String,

    @SerialName("name")
    val name: String
)