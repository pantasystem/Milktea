package net.pantasystem.milktea.data.model.group

import net.pantasystem.milktea.data.model.Entity
import net.pantasystem.milktea.data.model.EntityId
import net.pantasystem.milktea.data.model.users.User
import kotlinx.datetime.Instant

data class Group(
    val id: Id,
    val createdAt: Instant,
    val name: String,
    val ownerId: User.Id,
    val userIds: List<User.Id>
) : Entity {
    data class Id(val accountId: Long, val groupId: String) : EntityId
}