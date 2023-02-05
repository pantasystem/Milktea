package net.pantasystem.milktea.data.infrastructure

import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.api.misskey.groups.GroupDTO
import net.pantasystem.milktea.api.misskey.list.UserListDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.User

fun FilePropertyDTO.toFileProperty(account: Account): FileProperty {
    return FileProperty(
        id = FileProperty.Id(account.accountId, id),
        name = name,
        createdAt = createdAt,
        type = type,
        md5 = md5,
        size = size ?: 0,
        userId = userId?.let { User.Id(account.accountId, userId!!) },
        folderId = folderId,
        comment = comment,
        isSensitive = isSensitive ?: false,
        url = getUrl(account.normalizedInstanceDomain),
        thumbnailUrl = getThumbnailUrl(account.normalizedInstanceDomain),
        blurhash = blurhash,
        properties = properties?.let {
            FileProperty.Properties(it.width, it.height)
        }
    )
}


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