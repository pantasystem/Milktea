package net.pantasystem.milktea.model.list

import net.pantasystem.milktea.model.Entity
import net.pantasystem.milktea.model.EntityId
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.user.User

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