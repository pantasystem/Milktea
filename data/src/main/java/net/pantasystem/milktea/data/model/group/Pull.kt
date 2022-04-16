package net.pantasystem.milktea.data.model.group

import net.pantasystem.milktea.data.model.users.User

data class Pull (
    val groupId: Group.Id,
    val userId: User.Id,
)