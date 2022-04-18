package net.pantasystem.milktea.model.group

data class Pull (
    val groupId: Group.Id,
    val userId: net.pantasystem.milktea.model.user.User.Id,
)