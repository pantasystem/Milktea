package jp.panta.misskeyandroidclient.api.groups

data class DeleteGroup(
    val i: String,
    val groupId: String
)

data class CreateGroup(
    val i: String,
    val name: String
)

data class AcceptInvitation(
    val i: String,
    val invitationId: String
)

data class RejectInvitation(
    val i: String,
    val invitationId: String
)

data class InviteUser(
    val i: String,
    val userId: String
)

data class RemoveUser(
    val i: String,
    val userId: String
)

data class ShowGroup(
    val i: String,
    val groupId: String
)

data class TransferGroup(
    val i: String,
    val groupId: String,
    val userId: String
)

data class UpdateGroup(
    val i: String,
    val groupId: String,
    val name: String
)