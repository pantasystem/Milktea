package net.pantasystem.milktea.data.infrastructure.user.db

import androidx.room.*
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.User

@Entity(
    tableName = "user",
    foreignKeys = [
        ForeignKey(
            parentColumns = ["accountId"],
            childColumns = ["accountId"],
            entity = Account::class,
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            "serverId", "accountId", unique = true
        ),
        Index(
            "userName"
        ),
        Index("accountId"),
        Index("host")
    ]
)
data class UserRecord(
    val serverId: String,
    val accountId: Long,
    val userName: String,
    val name: String?,
    val avatarUrl: String?,
    val isCat: Boolean?,
    val isBot: Boolean?,
    val host: String,
    val isSameHost: Boolean,
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
)

@Entity(
    tableName = "user_detailed_state",
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["userId"],
            entity = UserRecord::class,
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId", unique = true),
    ]
)
data class UserDetailedStateRecord(
    val description: String?,
    val followersCount: Int?,
    val followingCount: Int?,
    val hostLower: String?,
    val notesCount: Int?,
    val bannerUrl: String?,
    val url: String?,
    val isFollowing: Boolean,
    val isFollower: Boolean,
    val isBlocking: Boolean,
    val isMuting: Boolean,
    val hasPendingFollowRequestFromYou: Boolean,
    val hasPendingFollowRequestToYou: Boolean,
    val isLocked: Boolean,
    @PrimaryKey(autoGenerate = false) val userId: Long
)

@Entity(
    tableName = "user_emoji",
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            entity = UserRecord::class,
            childColumns = ["userId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(
            "name", "userId", unique = true
        ),
        Index("userId")
    ]
)
data class UserEmojiRecord(
    val name: String,
    val url: String?,
    val uri: String?,
    val userId: Long,
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
) {
    fun toModel(): Emoji {
        return Emoji(
            name = name,
            url = url,
            uri = uri,
        )
    }
}

@Entity(
    tableName = "pinned_note_id",
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["userId"],
            entity = UserRecord::class,
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("noteId", "userId", unique = true),
        Index("userId")
    ]
)
data class PinnedNoteIdRecord(
    val noteId: String,
    val userId: Long,
    @PrimaryKey(autoGenerate = true) val id: Long
)

data class UserRelated(
    @Embedded val user: UserRecord,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val emojis: List<UserEmojiRecord>,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val detail: UserDetailedStateRecord?,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val pinnedNoteIds: List<PinnedNoteIdRecord>
) {
    fun toModel(): User {
        if (detail == null) {
            return User.Simple(
                id = User.Id(
                    user.accountId,
                    user.serverId,
                ),
                userName = user.userName,
                avatarUrl = user.avatarUrl,
                emojis = emojis.map {
                    it.toModel()
                },
                host = user.host,
                isBot = user.isBot,
                isCat = user.isCat,
                isSameHost = user.isSameHost,
                name = user.name,
                nickname = null,
            )
        } else {
            return User.Detail(
                id = User.Id(
                    user.accountId,
                    user.serverId,
                ),
                userName = user.userName,
                avatarUrl = user.avatarUrl,
                emojis = emojis.map {
                    it.toModel()
                },
                host = user.host,
                isBot = user.isBot,
                isCat = user.isCat,
                isSameHost = user.isSameHost,
                name = user.name,
                nickname = null,
                bannerUrl = detail.bannerUrl,
                description = detail.description,
                followingCount = detail.followingCount,
                followersCount = detail.followersCount,
                isFollowing = detail.isFollowing,
                isFollower = detail.isFollower,
                isBlocking = detail.isBlocking,
                isLocked = detail.isLocked,
                isMuting = detail.isMuting,
                hasPendingFollowRequestFromYou = detail.hasPendingFollowRequestFromYou,
                hasPendingFollowRequestToYou = detail.hasPendingFollowRequestToYou,
                hostLower = detail.hostLower,
                notesCount = detail.notesCount,
                pinnedNoteIds = pinnedNoteIds.map {
                    Note.Id(user.accountId, it.noteId)
                },
                url = detail.url,
            )
        }
    }
}