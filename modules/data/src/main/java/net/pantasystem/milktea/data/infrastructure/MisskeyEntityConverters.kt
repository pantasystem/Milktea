package net.pantasystem.milktea.data.infrastructure

import net.pantasystem.milktea.api.misskey.groups.GroupDTO
import net.pantasystem.milktea.api.misskey.list.UserListDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.User


fun UserListDTO.toEntity(account: Account): UserList {
    return UserList(
        UserList.Id(account.accountId, id),
        createdAt,
        name,
        userIds.map {
            User.Id(account.accountId, it)
        }
    )
}



fun GroupDTO.toGroup(accountId: Long): Group {
    return Group(
        Group.Id(accountId, id),
        createdAt,
        name,
        User.Id(accountId, ownerId),
        userIds.map {
            User.Id(accountId, it)
        }
    )
}


data class NoteRelationEntities(
    val note: Note,
    val notes: List<Note>,
    val users: List<User>,
    val files: List<FileProperty>
)