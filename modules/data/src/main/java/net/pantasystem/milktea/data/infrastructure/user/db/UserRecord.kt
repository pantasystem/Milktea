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
    @ColumnInfo(name = "serverId")
    val serverId: String,

    @ColumnInfo(name = "accountId")
    val accountId: Long,

    @ColumnInfo(name = "userName")
    val userName: String,

    @ColumnInfo(name = "name")
    val name: String?,

    @ColumnInfo(name = "avatarUrl")
    val avatarUrl: String?,

    @ColumnInfo(name = "isCat")
    val isCat: Boolean?,

    @ColumnInfo(name = "isBot")
    val isBot: Boolean?,

    @ColumnInfo(name = "host")
    val host: String,

    @ColumnInfo(name = "isSameHost")
    val isSameHost: Boolean,

    @ColumnInfo(name = "avatarBlurhash")
    val avatarBlurhash: String?,

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
) {
    companion object {
        fun from(user: User): UserRecord {
            return UserRecord(
                accountId = user.id.accountId,
                serverId = user.id.id,
                avatarUrl = user.avatarUrl,
                host = user.host,
                isBot = user.isBot,
                isCat = user.isCat,
                isSameHost = user.isSameHost,
                name = user.name,
                userName = user.userName,
                avatarBlurhash = user.avatarBlurhash,
            )
        }
    }
}

@Entity(
    tableName = "user_info_state",
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
data class UserInfoStateRecord(
    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "followersCount")
    val followersCount: Int?,

    @ColumnInfo(name = "followingCount")
    val followingCount: Int?,

    @ColumnInfo(name = "hostLower")
    val hostLower: String?,

    @ColumnInfo(name = "notesCount")
    val notesCount: Int?,

    @ColumnInfo(name = "bannerUrl")
    val bannerUrl: String?,

    @ColumnInfo(name = "url")
    val url: String?,

    @ColumnInfo(name = "isLocked")
    val isLocked: Boolean,

    @ColumnInfo(name = "birthday")
    val birthday: LocalDate?,

    @ColumnInfo(name = "createdAt")
    val createdAt: Instant?,

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Instant?,

    @ColumnInfo(name = "publicReactions")
    val publicReactions: Boolean?,

    @ColumnInfo(name = "userId")
    @PrimaryKey(autoGenerate = false)
    val userId: Long
)

@Entity(
    tableName = "user_related_state",
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
data class UserRelatedStateRecord(
    @ColumnInfo(name = "isFollowing")
    val isFollowing: Boolean,

    @ColumnInfo(name = "isFollower")
    val isFollower: Boolean,

    @ColumnInfo(name = "isBlocking")
    val isBlocking: Boolean,

    @ColumnInfo(name = "isMuting")
    val isMuting: Boolean,

    @ColumnInfo(name = "hasPendingFollowRequestFromYou")
    val hasPendingFollowRequestFromYou: Boolean,

    @ColumnInfo(name = "hasPendingFollowRequestToYou")
    val hasPendingFollowRequestToYou: Boolean,

    @ColumnInfo(name = "userId")
    @PrimaryKey(autoGenerate = false) val userId: Long
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
    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "followersCount")
    val followersCount: Int?,

    @ColumnInfo(name = "followingCount")
    val followingCount: Int?,

    @ColumnInfo(name = "hostLower")
    val hostLower: String?,

    @ColumnInfo(name = "notesCount")
    val notesCount: Int?,

    @ColumnInfo(name = "bannerUrl")
    val bannerUrl: String?,

    @ColumnInfo(name = "url")
    val url: String?,

    @ColumnInfo(name = "isFollowing")
    val isFollowing: Boolean,

    @ColumnInfo(name = "isFollower")
    val isFollower: Boolean,

    @ColumnInfo(name = "isBlocking")
    val isBlocking: Boolean,

    @ColumnInfo(name = "isMuting")
    val isMuting: Boolean,

    @ColumnInfo(name = "hasPendingFollowRequestFromYou")
    val hasPendingFollowRequestFromYou: Boolean,

    @ColumnInfo(name = "hasPendingFollowRequestToYou")
    val hasPendingFollowRequestToYou: Boolean,

    @ColumnInfo(name = "isLocked")
    val isLocked: Boolean,

    @ColumnInfo(name = "birthday")
    val birthday: LocalDate?,

    @ColumnInfo(name = "createdAt")
    val createdAt: Instant?,

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Instant?,

    @ColumnInfo(name = "publicReactions")
    val publicReactions: Boolean?,

    @ColumnInfo(name = "userId")
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
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "url")
    val url: String?,

    @ColumnInfo(name = "uri")
    val uri: String?,

    @ColumnInfo(name = "userId")
    val userId: Long,

    @ColumnInfo(name = "aspectRatio")
    val aspectRatio: Float? = null,

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
) {
    fun toModel(): Emoji {
        return Emoji(
            name = name,
            url = url,
            uri = uri,
            aspectRatio = aspectRatio,
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
    @ColumnInfo(name = "faviconUrl")
    val faviconUrl: String?,

    @ColumnInfo(name = "iconUrl")
    val iconUrl: String?,

    @ColumnInfo(name = "name")
    val name: String?,

    @ColumnInfo(name = "softwareName")
    val softwareName: String?,

    @ColumnInfo(name = "softwareVersion")
    val softwareVersion: String?,

    @ColumnInfo(name = "themeColor")
    val themeColor: String?,

    @ColumnInfo(name = "userId")
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
    @ColumnInfo(name = "noteId")
    val noteId: String,

    @ColumnInfo(name = "userId")
    val userId: Long,

    @ColumnInfo(name = "id")
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
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "value")
    val value: String,

    @ColumnInfo(name = "userId")
    val userId: Long,

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true) val id: Long = 0L
)

@DatabaseView(
    "select user.*, nicknames.nickname from user left join nicknames on user.userName = nicknames.username and user.host = nicknames.host",
    viewName = "user_view"
)
data class UserView(
    @ColumnInfo(name = "serverId")
    val serverId: String,

    @ColumnInfo(name = "accountId")
    val accountId: Long,

    @ColumnInfo(name = "userName")
    val userName: String,

    @ColumnInfo(name = "name")
    val name: String?,

    @ColumnInfo(name = "avatarUrl")
    val avatarUrl: String?,

    @ColumnInfo(name = "isCat")
    val isCat: Boolean?,

    @ColumnInfo(name = "isBot")
    val isBot: Boolean?,

    @ColumnInfo(name = "host")
    val host: String,

    @ColumnInfo(name = "isSameHost")
    val isSameHost: Boolean,

    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "nickname")
    val nickname: String?,

    @ColumnInfo(name = "avatarBlurhash")
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
    val info: UserInfoStateRecord?,

    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val related: UserRelatedStateRecord?,

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
        if (info == null) {
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
                instance = instanceInfo,
                avatarBlurhash = user.avatarBlurhash,
                info = info.let { info ->
                    User.Info(
                        bannerUrl = info.bannerUrl,
                        description = info.description,
                        followingCount = info.followingCount,
                        followersCount = info.followersCount,
                        isLocked = info.isLocked,
                        hostLower = info.hostLower,
                        notesCount = info.notesCount,
                        pinnedNoteIds = pinnedNoteIds.map {
                            Note.Id(user.accountId, it.noteId)
                        },
                        url = info.url,
                        birthday = info.birthday,
                        createdAt = info.createdAt,
                        updatedAt = info.updatedAt,
                        fields = fields?.map {
                            User.Field(it.name, it.value)
                        } ?: emptyList(),
                        isPublicReactions = info.publicReactions ?: false,
                    )
                },
                related = related?.let { related ->
                    User.Related(
                        isFollowing = related.isFollowing,
                        isFollower = related.isFollower,
                        isBlocking = related.isBlocking,
                        isMuting = related.isMuting,
                        hasPendingFollowRequestFromYou = related.hasPendingFollowRequestFromYou,
                        hasPendingFollowRequestToYou = related.hasPendingFollowRequestToYou,
                    )
                }
            )
        }
    }
}