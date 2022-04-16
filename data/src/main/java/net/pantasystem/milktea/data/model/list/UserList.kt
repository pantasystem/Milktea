package net.pantasystem.milktea.data.model.list

import net.pantasystem.milktea.data.model.Entity
import net.pantasystem.milktea.data.model.EntityId
import net.pantasystem.milktea.data.model.users.User
import kotlinx.datetime.Instant

data class UserList(
    val id: Id,
    val createdAt: Instant,
    val name: String,
    val userIds: List<User.Id>
) : Entity {

    data class Id(
        val accountId: Long,
        val userListId: String
    ) : EntityId
}