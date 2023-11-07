package net.pantasystem.milktea.data.infrastructure

import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.api.misskey.groups.GroupDTO
import net.pantasystem.milktea.api.misskey.list.UserListDTO
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.list.UserList
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


data class NoteDTOUnpacked(
    val note: NoteDTO,
    val notes: List<NoteDTO>,
    val users: List<User>,
    val files: List<FileProperty>
)

data class TootDTOUnpacked(
    val toot: TootStatusDTO,
    val toots: List<TootStatusDTO>,
    val users: List<User>,
    val files: List<FileProperty>
)