package jp.panta.misskeyandroidclient.api.misskey.groups

data class DeleteGroupDTO(
    val i: String,
    val groupId: String
)

data class CreateGroupDTO(
    val i: String,
    val name: String
)

data class AcceptInvitationDTO(
    val i: String,
    val invitationId: String
)

data class RejectInvitationDTO(
    val i: String,
    val invitationId: String
)

data class InviteUserDTO(
    val i: String,
    val userId: String
)

data class RemoveUserDTO(
    val i: String,
    val userId: String,
    val groupId: String
)

data class ShowGroupDTO(
    val i: String,
    val groupId: String
)

data class TransferGroupDTO(
    val i: String,
    val groupId: String,
    val userId: String
)

data class UpdateGroupDTO(
    val i: String,
    val groupId: String,
    val name: String
)