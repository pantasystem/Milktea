package net.pantasystem.milktea.data.infrastructure.user.db

import androidx.room.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import net.pantasystem.milktea.data.infrastructure.account.db.AccountRecord
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.nickname.UserNickname

@Entity(
    tableName = "user",
    foreignKeys = [
        ForeignKey(
            parentColumns = ["accountId"],
            childColumns = ["accountId"],
            entity = AccountRecord::class,
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
    val avatarBlurhash: String?,
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
    val birthday: LocalDate?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val publicReactions: Boolean?,
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
    tableName = "user_instance_info",
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            entity = UserRecord::class,
            childColumns = ["userId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId")
    ]
)
data class UserInstanceInfoRecord(
    val faviconUrl: String?,
    val iconUrl: String?,
    val name: String?,
    val softwareName: String?,
    val softwareVersion: String?,
    val themeColor: String?,
    @PrimaryKey(autoGenerate = false) val userId: Long
)

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

@Entity(
    tableName = "user_profile_field",
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["userId"],
            entity = UserRecord::class,
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("userId")
    ]
)
data class UserProfileFieldRecord(
    val name: String,
    val value: String,
    val userId: Long,
    @PrimaryKey(autoGenerate = true) val id: Long = 0L
)

@DatabaseView(
    "select user.*, nicknames.nickname from user left join nicknames on user.userName = nicknames.username and user.host = nicknames.host",
    viewName = "user_view"
)
data class UserView(
    val serverId: String,
    val accountId: Long,
    val userName: String,
    val name: String?,
    val avatarUrl: String?,
    val isCat: Boolean?,
    val isBot: Boolean?,
    val host: String,
    val isSameHost: Boolean,
    val id: Long,
    val nickname: String?,
    val avatarBlurhash: String?
)

interface HasUserModel {
    fun toModel(): User
}

data class UserSimpleRelated(
    @Embedded val user: UserView,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val emojis: List<UserEmojiRecord>,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val instance: UserInstanceInfoRecord?,
) : HasUserModel {
    override fun toModel(): User.Simple {
        val instanceInfo = instance?.let {
            User.InstanceInfo(
                faviconUrl = it.faviconUrl,
                iconUrl = it.iconUrl,
                name = it.name,
                softwareName = it.softwareName,
                softwareVersion = it.softwareVersion,
                themeColor = it.themeColor
            )
        }
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
            nickname = user.nickname?.let {
                UserNickname(
                    id = UserNickname.Id(user.userName, user.host),
                    name = user.nickname
                )
            },
            instance = instanceInfo,
            avatarBlurhash = user.avatarBlurhash,
        )
    }
}

data class UserRelated(
    @Embedded val user: UserView,
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
    val pinnedNoteIds: List<PinnedNoteIdRecord>,

    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val instance: UserInstanceInfoRecord?,

    @Relation(
        parentColumn = "id",
        entityColumn = "userId",
    )
    val fields: List<UserProfileFieldRecord>?

) : HasUserModel {
    override fun toModel(): User {
        val instanceInfo = instance?.let {
            User.InstanceInfo(
                faviconUrl = it.faviconUrl,
                iconUrl = it.iconUrl,
                name = it.name,
                softwareName = it.softwareName,
                softwareVersion = it.softwareVersion,
                themeColor = it.themeColor
            )
        }
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
                nickname = user.nickname?.let {
                    UserNickname(
                        id = UserNickname.Id(user.userName, user.host),
                        name = user.nickname
                    )
                },
                instance = instanceInfo,
                avatarBlurhash = user.avatarBlurhash,
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
                nickname = user.nickname?.let {
                    UserNickname(
                        id = UserNickname.Id(user.userName, user.host),
                        name = user.nickname
                    )
                },
                related = User.Related(
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
                    birthday = detail.birthday,
                    createdAt = detail.createdAt,
                    updatedAt = detail.updatedAt,
                    fields = fields?.map {
                        User.Field(it.name, it.value)
                    } ?: emptyList(),
                    isPublicReactions = detail.publicReactions ?: false,
                ),
                instance = instanceInfo,
                avatarBlurhash = user.avatarBlurhash,
            )
        }
    }
}