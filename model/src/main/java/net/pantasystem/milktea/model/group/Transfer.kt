package net.pantasystem.milktea.model.group

data class Transfer(
    val groupId: Group.Id,
    val userId: net.pantasystem.milktea.model.user.User.Id
)