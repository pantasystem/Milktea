package net.pantasystem.milktea.data.infrastructure.user.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import net.pantasystem.milktea.model.account.Account

@Entity(
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
    @PrimaryKey(autoGenerate = true) val id: Long,
)

@Entity(
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
    @PrimaryKey(autoGenerate = true) val id: Long,
)

@Entity(
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