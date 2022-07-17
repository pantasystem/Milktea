package net.pantasystem.milktea.model.antenna

import net.pantasystem.milktea.model.Entity
import net.pantasystem.milktea.model.EntityId
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.list.UserList

data class Antenna (
    val id: Id,
    val name: String,
    val src: AntennaSource,
    val userListId: UserList.Id?,
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

sealed interface AntennaSource {
    object Home : AntennaSource
    object All : AntennaSource
    object Users : AntennaSource
    object List : AntennaSource
    object Group : AntennaSource
    companion object
}

fun AntennaSource.str(): String {
    return when(this) {
        AntennaSource.All -> "all"
        AntennaSource.Group -> "group"
        AntennaSource.Home -> "home"
        AntennaSource.List -> "list"
        AntennaSource.Users -> "users"
    }
}

fun AntennaSource.Companion.values(): List<AntennaSource> {
    return listOf(
        AntennaSource.Home,
        AntennaSource.All,
        AntennaSource.Users,
        AntennaSource.List,
        AntennaSource.Group,
    )
}

fun AntennaSource.Companion.from(src: String): AntennaSource {
    return when(src) {
        "all" -> AntennaSource.All
        "group" -> AntennaSource.Group
        "home" -> AntennaSource.Home
        "list" -> AntennaSource.List
        "users" -> AntennaSource.Users
        else -> AntennaSource.All
    }
}