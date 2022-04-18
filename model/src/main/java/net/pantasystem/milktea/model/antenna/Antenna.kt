package net.pantasystem.milktea.model.antenna

import net.pantasystem.milktea.model.Entity
import net.pantasystem.milktea.model.EntityId
import net.pantasystem.milktea.model.group.Group

data class Antenna (
    val id: Id,
    val name: String,
    val src: String,
    val userListId: String?,
    val userGroupId: Group.Id?,
    val keywords: List<List<String>>,
    val excludeKeywords: List<List<String>>,
    val users: List<String>,
    val caseSensitive: Boolean,
    val withFile: Boolean,
    val withReplies: Boolean,
    val notify: Boolean,
    val hasUnreadNote: Boolean
) : Entity {
    data class Id(
        val accountId: Long,
        val antennaId: String
    ) : EntityId
}