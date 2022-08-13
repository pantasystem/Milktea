package net.pantasystem.milktea.model.group

import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.Entity
import net.pantasystem.milktea.model.EntityId
import net.pantasystem.milktea.model.user.User

data class Group(
    val id: Id,
    val createdAt: Instant,
    val name: String,
    val ownerId: User.Id,
    val userIds: List<User.Id>
) : Entity {
    data class Id(val accountId: Long, val groupId: String) : EntityId
}

data class GroupMember(
    val userId: User.Id,
    val avatarUrl: String?
)

data class GroupWithMember(
    val group: Group,
    val members: List<GroupMember>
)