package net.pantasystem.milktea.data.infrastructure

import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.api.misskey.groups.GroupDTO
import net.pantasystem.milktea.api.misskey.list.UserListDTO
import net.pantasystem.milktea.api.misskey.messaging.MessageDTO
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.PollDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.group.InvitationId
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.messaging.Message
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.model.notification.*
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

fun MessageDTO.entities(account: Account): Pair<Message, List<User>> {
    val list = mutableListOf<User>()
    val id = Message.Id(account.accountId, id)
    list.add(user.toUser(account, false))
    val message = if(groupId == null) {
        require(recipientId != null)
        Message.Direct(
            id,
            createdAt,
            text,
            User.Id(account.accountId, userId),
            fileId,
            file?.toFileProperty(account),
            isRead,
            emojis?: emptyList(),
            recipientId = User.Id(account.accountId, recipientId!!)
        )
    }else{
        Message.Group(
            id,
            createdAt,
            text,
            User.Id(account.accountId, userId),
            fileId,
            file?.toFileProperty(account),
            isRead = reads?.contains(account.remoteId) ?: false,
            emojis?: emptyList(),
            Group.Id(account.accountId, groupId!!),
            reads = reads?.map {
                User.Id(account.accountId, it)
            }?: emptyList()
        )
    }
    return message to list
}

fun NoteDTO.toEntities(account: Account): NoteRelationEntities {
    val dtoList = mutableListOf<NoteDTO>()
    dtoList.add(this)


    if(this.reply != null){

        dtoList.add(this.reply!!)
    }
    if(this.reNote != null){
        dtoList.add(reNote!!)
    }

    val note = this.toNote(account)
    val users = mutableListOf<User>()
    val notes = mutableListOf<Note>()
    val files = mutableListOf<FileProperty>()

    pickEntities(account, notes, users, files)
    return NoteRelationEntities(
        note = note,
        notes = notes,
        users = users,
        files = files
    )
}

private fun NoteDTO.pickEntities(account: Account, notes: MutableList<Note>, users: MutableList<User>, files: MutableList<FileProperty>) {
    val (note, user) = this.toNoteAndUser(account)
    notes.add(note)
    users.add(user)
    files.addAll(
        this.files?.map {
            it.toFileProperty(account)
        }?: emptyList()
    )
    if(this.reply != null) {
        this.reply!!.pickEntities(account, notes, users, files)
    }

    if(this.reNote != null) {
        this.reNote!!.pickEntities(account, notes, users, files)
    }
}

fun PollDTO?.toPoll(): Poll? {
    return this?.let { dto ->
        Poll(
            multiple = dto.multiple,
            expiresAt = dto.expiresAt,
            choices = choices.mapIndexed { index, value ->
                Poll.Choice(
                    index = index,
                    text = value.text,
                    isVoted = value.isVoted,
                    votes = value.votes
                )
            }
        )
    }
}


fun NoteDTO.toNote(account: Account): Note {
    val visibility = Visibility(this.visibility?: "public", isLocalOnly = localOnly?: false, visibleUserIds = visibleUserIds?.map { id ->
        User.Id(account.accountId, id)
    }?: emptyList())
    return Note(
        id = Note.Id(account.accountId, this.id),
        createdAt = this.createdAt,
        text = this.text,
        cw = this.cw,
        userId = User.Id(account.accountId, this.userId, ),
        replyId = this.replyId?.let{ Note.Id(account.accountId, this.replyId!!) },
        renoteId = this.renoteId?.let{ Note.Id(account.accountId, this.renoteId!!) },
        viaMobile = this.viaMobile,
        visibility = visibility,
        localOnly = this.localOnly,
        emojis = this.emojis,
        app = this.app?.toModel(),
        fileIds = this.fileIds?.map { FileProperty.Id(account.accountId, it) },
        poll = this.poll?.toPoll(),
        reactionCounts = this.reactionCounts?.map{
            ReactionCount(reaction = it.key, it.value)
        }?: emptyList(),
        renoteCount = this.renoteCount,
        repliesCount = this.replyCount,
        uri = this.uri,
        url = this.url,
        visibleUserIds = this.visibleUserIds?.map{
            User.Id(account.accountId, it)
        }?: emptyList(),
        myReaction = this.myReaction,
        channelId = this.channelId?.let {
            Channel.Id(account.accountId, it)
        },
        type = Note.Type.Misskey,
    )
}


fun NoteDTO.toNoteAndUser(account: Account): Pair<Note, User> {
    val note = this.toNote(account)
    val user = this.user.toUser(account,false)
    return note to user
}


fun NotificationDTO.toNotification(account: Account): Notification {
    val id = Notification.Id(account.accountId, this.id)
    return when (this.type) {
        "follow" -> {
            FollowNotification(
                id, createdAt, User.Id(account.accountId, this.userId!!), isRead ?: true
            )
        }
        "followRequestAccepted" -> {
            FollowRequestAcceptedNotification(
                id, createdAt, User.Id(account.accountId, this.userId!!), isRead ?: true
            )
        }
        "receiveFollowRequest" -> {
            ReceiveFollowRequestNotification(
                id, createdAt, User.Id(account.accountId, this.userId!!), isRead ?: true
            )
        }
        "mention" -> {
            MentionNotification(
                id,
                createdAt,
                User.Id(account.accountId, this.userId!!),
                Note.Id(
                    account.accountId,
                    note?.id ?: throw IllegalStateException("noteId参照不能")
                ),
                isRead ?: true
            )
        }
        "reply" -> {
            ReplyNotification(
                id,
                createdAt,
                User.Id(account.accountId, this.userId!!),
                Note.Id(
                    account.accountId,
                    note?.id ?: throw IllegalStateException("noteId参照不能")
                ),
                isRead ?: true
            )
        }
        "renote" -> {
            RenoteNotification(
                id,
                createdAt,
                User.Id(account.accountId, this.userId!!),
                Note.Id(
                    account.accountId,
                    note?.id ?: throw IllegalStateException("noteId参照不能")
                ),
                isRead ?: true
            )
        }
        "quote" -> {
            QuoteNotification(
                id,
                createdAt,
                User.Id(account.accountId, this.userId!!),
                Note.Id(
                    account.accountId,
                    note?.id ?: throw IllegalStateException("noteId参照不能")
                ),
                isRead ?: true
            )
        }
        "reaction" -> {

            require(reaction != null) {
                "想定しないデータ=$this"
            }
            require(note != null)
            val n = note!!.toNote(account)
            ReactionNotification(
                id,
                createdAt,
                User.Id(account.accountId, this.userId!!),
                n.id,
                reaction!!,
                isRead ?: true
            )
        }
        "pollVote" -> {
            require(noteId != null || note != null)
            require(choice != null)
            PollVoteNotification(
                id,
                Note.Id(account.accountId, noteId ?: note?.id!!),
                createdAt,
                User.Id(account.accountId, this.userId!!),
                choice!!,
                isRead ?: true
            )
        }
        "pollEnded", "poll_finished" -> {
            require(note != null)
            PollEndedNotification(
                id,
                createdAt,
                isRead = isRead ?: true,
                Note.Id(account.accountId, note!!.id)
            )
        }
        "groupInvited" -> {
            require(invitation != null)
            require(userId != null)
            GroupInvitedNotification(
                id,
                isRead = isRead?: true,
                createdAt,
                group = invitation!!.group.toGroup(account.accountId),
                User.Id(account.accountId, userId!!),
                InvitationId(account.accountId, invitation!!.id),
            )
        }
        else -> {
            return UnknownNotification(
                id,
                createdAt,
                isRead ?: false,
                User.Id(account.accountId, this.userId!!),
                this.type
            )
        }
    }
}

fun UserDTO.toUser(account: Account, isDetail: Boolean = false): User {
    val instanceInfo = instance?.let {
        User.InstanceInfo(
            name = it.name,
            faviconUrl = it.faviconUrl,
            iconUrl = it.iconUrl,
            softwareName = it.softwareName,
            softwareVersion = it.softwareVersion,
            themeColor = it.themeColor
        )
    }
    if (isDetail) {

        return User.Detail(
            id = User.Id(account.accountId, this.id),
            avatarUrl = this.avatarUrl,
            emojis = this.emojis ?: emptyList(),
            isBot = this.isBot,
            isCat = this.isCat,
            name = this.name,
            userName = this.userName,
            host = this.host ?: account.getHost(),
            nickname = null,
            isSameHost = host == null,
            instance = instanceInfo,
            avatarBlurhash = avatarBlurhash,
            info = User.Info(
                bannerUrl = this.bannerUrl,
                description = this.description,
                followersCount = this.followersCount,
                followingCount = this.followingCount,
                url = this.url,
                hostLower = this.hostLower,
                notesCount = this.notesCount,
                pinnedNoteIds = this.pinnedNoteIds?.map {
                    Note.Id(account.accountId, it)
                },
                isLocked = isLocked ?: false,
                birthday = birthday,
                createdAt = createdAt,
                updatedAt = updatedAt,
                fields = fields?.map {
                    User.Field(it.name, it.value)
                }?: emptyList(),
                isPublicReactions = publicReactions ?: false,
            ),
            related = User.Related(
                isFollowing = this.isFollowing ?: false,
                isFollower = this.isFollowed ?: false,
                isBlocking = this.isBlocking ?: false,
                isMuting = this.isMuted ?: false,
                hasPendingFollowRequestFromYou = hasPendingFollowRequestFromYou ?: false,
                hasPendingFollowRequestToYou = hasPendingFollowRequestToYou ?: false,
            )
        )
    } else {
        return User.Simple(
            id = User.Id(account.accountId, this.id),
            avatarUrl = this.avatarUrl,
            emojis = this.emojis ?: emptyList(),
            isBot = this.isBot,
            isCat = this.isCat,
            name = this.name,
            userName = this.userName,
            host = this.host ?: account.getHost(),
            nickname = null,
            isSameHost = host == null,
            instance = instanceInfo,
            avatarBlurhash = avatarBlurhash
        )
    }

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


suspend fun net.pantasystem.milktea.api.misskey.v12_75_0.GalleryPost.toEntity(
    account: Account,
    filePropertyDataSource: FilePropertyDataSource,
    userDataSource: net.pantasystem.milktea.model.user.UserDataSource
): GalleryPost {
    filePropertyDataSource.addAll(files.map {
        it.toFileProperty(account)
    })
    // NOTE: API上ではdetailだったが実際に受信されたデータはSimpleだったのでfalse
    userDataSource.add(user.toUser(account, false))
    if (this.likedCount == null || this.isLiked == null) {

        return GalleryPost.Normal(
            GalleryPost.Id(account.accountId, this.id),
            createdAt,
            updatedAt,
            title,
            description,
            User.Id(account.accountId, userId),
            files.map {
                FileProperty.Id(account.accountId, it.id)
            },
            tags ?: emptyList(),
            isSensitive
        )
    } else {
        return GalleryPost.Authenticated(
            GalleryPost.Id(account.accountId, this.id),
            createdAt,
            updatedAt,
            title,
            description,
            User.Id(account.accountId, userId),
            files.map {
                FileProperty.Id(account.accountId, it.id)
            },
            tags ?: emptyList(),
            isSensitive,
            likedCount ?: 0,
            isLiked ?: false
        )
    }
}

data class NoteRelationEntities(
    val note: Note,
    val notes: List<Note>,
    val users: List<User>,
    val files: List<FileProperty>
)