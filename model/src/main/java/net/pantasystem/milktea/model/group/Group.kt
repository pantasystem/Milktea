package net.pantasystem.milktea.model.group

import net.pantasystem.milktea.model.Entity
import net.pantasystem.milktea.model.EntityId
import kotlinx.datetime.Instant

data class Group(
    val id: Id,
    val createdAt: Instant,
    val name: String,
    val ownerId: net.pantasystem.milktea.model.user.User.Id,
    val userIds: List<net.pantasystem.milktea.model.user.User.Id>
) : Entity {
    data class Id(val accountId: Long, val groupId: String) : EntityId
}