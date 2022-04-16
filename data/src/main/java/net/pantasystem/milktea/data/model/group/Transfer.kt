package net.pantasystem.milktea.data.model.group

import net.pantasystem.milktea.data.model.users.User

data class Transfer(
    val groupId: Group.Id,
    val userId: User.Id
)