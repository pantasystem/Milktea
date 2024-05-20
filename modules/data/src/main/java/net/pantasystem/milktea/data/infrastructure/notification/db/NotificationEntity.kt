package net.pantasystem.milktea.data.infrastructure.notification.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.datetime.Instant
import net.pantasystem.milktea.data.infrastructure.account.db.AccountRecord
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.group.InvitationId
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.notification.FavoriteNotification
import net.pantasystem.milktea.model.notification.FollowNotification
import net.pantasystem.milktea.model.notification.FollowRequestAcceptedNotification
import net.pantasystem.milktea.model.notification.GroupInvitedNotification
import net.pantasystem.milktea.model.notification.MentionNotification
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.notification.PollEndedNotification
import net.pantasystem.milktea.model.notification.PollVoteNotification
import net.pantasystem.milktea.model.notification.PostNotification
import net.pantasystem.milktea.model.notification.QuoteNotification
import net.pantasystem.milktea.model.notification.ReactionNotification
import net.pantasystem.milktea.model.notification.ReceiveFollowRequestNotification
import net.pantasystem.milktea.model.notification.RenoteNotification
import net.pantasystem.milktea.model.notification.ReplyNotification
import net.pantasystem.milktea.model.notification.UnknownNotification
import net.pantasystem.milktea.model.user.User

@Entity(
    tableName = "notifications",
    foreignKeys = [
        // account_id
        ForeignKey(
            entity = AccountRecord::class,
            parentColumns = ["accountId"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
    ],
    indices = [
        // notification_id
        Index(
            "account_id"
        )
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = "notification_id") val notificationId: String,
    @ColumnInfo(name = "account_id") val accountId: Long,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "type") val type: String
) {
    companion object {
        fun makeId(accountId: Long, notificationId: String): String {
            return "$accountId:$notificationId"
        }
    }
}

@Entity(
    tableName = "follow_notifications",
    foreignKeys = [
        ForeignKey(
            entity = NotificationEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
)
data class FollowNotificationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
)

@Entity(
    tableName = "note_notifications",
    foreignKeys = [
        ForeignKey(
            entity = NotificationEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
)
data class NoteNotificationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo("note_id") val noteId: String,
    @ColumnInfo("user_id") val userId: String,
)

@Entity(
    tableName = "reaction_notifications",
    foreignKeys = [
        ForeignKey(
            entity = NotificationEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
)
data class ReactionNotificationEntity(
    @PrimaryKey val id: String,
    val reaction: String
)

@Entity(
    tableName = "poll_vote_notifications",
    foreignKeys = [
        ForeignKey(
            entity = NotificationEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
)
data class PollVoteNotificationEntity(
    @PrimaryKey val id: String,
    val choice: Int,
)

@Entity(
    tableName = "poll_ended_notifications",
)
data class PollEndedNotificationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo("note_id") val noteId: String,
)

// group invited
@Entity(
    tableName = "group_invited_notifications",
    foreignKeys = [
        ForeignKey(
            entity = NotificationEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
)
data class GroupInvitedNotificationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo("group_id") val groupId: String,
    @ColumnInfo("group_name") val groupName: String,
    @ColumnInfo("group_owner_id") val groupOwnerId: String,
    @ColumnInfo("group_created_at") val groupCreatedAt: Instant,
    @ColumnInfo("invitation_id") val invitationId: String,
)

@Entity(
    tableName = "unknown_notifications",
    foreignKeys = [
        ForeignKey(
            entity = NotificationEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
)
data class UnknownNotificationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo("raw_type") val rawType: String
)

data class NotificationWithDetails(
    @Embedded val notification: NotificationEntity,
    @Relation(
        entity = FollowNotificationEntity::class,
        parentColumn = "id",
        entityColumn = "id"
    )
    val followNotification: FollowNotificationEntity?,

    @Relation(
        entity = NoteNotificationEntity::class,
        parentColumn = "id",
        entityColumn = "id"
    )
    val noteNotification: NoteNotificationEntity?,

    @Relation(
        entity = ReactionNotificationEntity::class,
        parentColumn = "id",
        entityColumn = "id"
    )
    val reactionNotification: ReactionNotificationEntity?,

    @Relation(
        entity = PollVoteNotificationEntity::class,
        parentColumn = "id",
        entityColumn = "id"
    )
    val pollVoteNotification: PollVoteNotificationEntity?,

    @Relation(
        entity = GroupInvitedNotificationEntity::class,
        parentColumn = "id",
        entityColumn = "id"
    )
    val groupInvitedNotification: GroupInvitedNotificationEntity?,

    @Relation(
        entity = UnknownNotificationEntity::class,
        parentColumn = "id",
        entityColumn = "id"
    )
    val unknownNotification: UnknownNotificationEntity?,

    @Relation(
        entity = UnreadNotification::class,
        parentColumn = "notification_id",
        entityColumn = "notificationId",
    )
    val unreadNotification: List<UnreadNotification>?,

    @Relation(
        entity = PollEndedNotificationEntity::class,
        parentColumn = "id",
        entityColumn = "id"
    )
    val pollEndedNotification: PollEndedNotificationEntity?,
) {

    companion object {
        fun fromModel(model: Notification): NotificationWithDetails {
            val notificationEntity = NotificationEntity(
                NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                model.id.notificationId,
                model.id.accountId,
                model.createdAt,
                NotificationType.modelOf(model).value
            )

            fun unreadNotification(): List<UnreadNotification> {
                return if (model.isRead) {
                    listOf(
                        UnreadNotification(
                            model.id.accountId,
                            model.id.notificationId
                        )
                    )
                } else {
                    emptyList()
                }
            }
            return when (model) {
                is FavoriteNotification -> NotificationWithDetails(
                    notificationEntity,
                    null,
                    NoteNotificationEntity(
                        NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                        model.noteId.noteId,
                        model.userId.id,
                    ),
                    null,
                    null,
                    null,
                    null,
                    unreadNotification(),
                    null,
                )

                is FollowNotification -> {
                    NotificationWithDetails(
                        notificationEntity,
                        FollowNotificationEntity(
                            NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                            model.userId.id
                        ),
                        null,
                        null,
                        null,
                        null,
                        null,
                        unreadNotification(),
                        null,
                    )
                }

                is FollowRequestAcceptedNotification -> {
                    NotificationWithDetails(
                        notificationEntity,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        unreadNotification(),
                        null,
                    )
                }

                is GroupInvitedNotification -> {
                    NotificationWithDetails(
                        notificationEntity,
                        null,
                        null,
                        null,
                        null,
                        GroupInvitedNotificationEntity(
                            NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                            groupId = model.group.id.groupId,
                            groupName = model.group.name,
                            groupOwnerId = model.group.ownerId.id,
                            groupCreatedAt = model.group.createdAt,
                            invitationId = model.invitationId.invitationId,
                        ),
                        null,
                        unreadNotification(),
                        null,
                    )
                }

                is MentionNotification -> {
                    NotificationWithDetails(
                        notificationEntity,
                        null,
                        NoteNotificationEntity(
                            NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                            model.noteId.noteId,
                            model.userId.id,
                        ),
                        null,
                        null,
                        null,
                        null,
                        unreadNotification(),
                        null,
                    )
                }

                is PollEndedNotification -> {
                    NotificationWithDetails(
                        notificationEntity,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        unreadNotification(),
                        PollEndedNotificationEntity(
                            NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                            model.noteId.noteId,
                        ),
                    )
                }

                is PollVoteNotification -> {
                    NotificationWithDetails(
                        notificationEntity,
                        null,
                        null,
                        null,
                        PollVoteNotificationEntity(
                            NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                            model.choice,
                        ),
                        null,
                        null,
                        unreadNotification(),
                        null,
                    )
                }

                is PostNotification -> {
                    NotificationWithDetails(
                        notificationEntity,
                        null,
                        NoteNotificationEntity(
                            NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                            model.noteId.noteId,
                            model.userId.id,
                        ),
                        null,
                        null,
                        null,
                        null,
                        unreadNotification(),
                        null,
                    )
                }

                is QuoteNotification -> {
                    NotificationWithDetails(
                        notificationEntity,
                        null,
                        NoteNotificationEntity(
                            NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                            model.noteId.noteId,
                            model.userId.id,
                        ),
                        null,
                        null,
                        null,
                        null,
                        unreadNotification(),
                        null,
                    )
                }

                is ReactionNotification -> {
                    NotificationWithDetails(
                        notificationEntity,
                        null,
                        NoteNotificationEntity(
                            NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                            model.noteId.noteId,
                            model.userId.id,
                        ),
                        ReactionNotificationEntity(
                            NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                            model.reaction,
                        ),
                        null,
                        null,
                        null,
                        unreadNotification(),
                        null,
                    )
                }

                is ReceiveFollowRequestNotification -> {
                    NotificationWithDetails(
                        notificationEntity,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        unreadNotification(),
                        null,
                    )
                }

                is RenoteNotification -> {
                    NotificationWithDetails(
                        notificationEntity,
                        null,
                        NoteNotificationEntity(
                            NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                            model.noteId.noteId,
                            model.userId.id,
                        ),
                        null,
                        null,
                        null,
                        null,
                        unreadNotification(),
                        null,
                    )
                }

                is ReplyNotification -> {
                    NotificationWithDetails(
                        notificationEntity,
                        null,
                        NoteNotificationEntity(
                            NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                            model.noteId.noteId,
                            model.userId.id,
                        ),
                        null,
                        null,
                        null,
                        null,
                        unreadNotification(),
                        null,
                    )
                }

                is UnknownNotification -> {
                    NotificationWithDetails(
                        notificationEntity,
                        null,
                        null,
                        null,
                        null,
                        null,
                        UnknownNotificationEntity(
                            NotificationEntity.makeId(model.id.accountId, model.id.notificationId),
                            model.rawType,
                        ),
                        unreadNotification(),
                        null,
                    )
                }
            }
        }
    }

    fun toModel(): Notification {
        val type = NotificationType.entries.firstOrNull { it.value == notification.type }
            ?: NotificationType.Unknown
        return when (type) {
            NotificationType.Follow -> {
                FollowNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    notification.createdAt,
                    User.Id(notification.accountId, followNotification!!.userId),
                    isRead()
                )
            }

            NotificationType.Favorite -> {
                FavoriteNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    notification.createdAt,
                    User.Id(notification.accountId, noteNotification!!.userId),
                    Note.Id(notification.accountId, noteNotification.noteId),
                    isRead(),
                )
            }

            NotificationType.FollowRequestAccepted -> {
                FollowRequestAcceptedNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    notification.createdAt,
                    User.Id(notification.accountId, noteNotification!!.userId),
                    isRead(),
                )
            }

            NotificationType.GroupInvited -> {
                GroupInvitedNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    isRead(),
                    notification.createdAt,
                    Group(
                        Group.Id(notification.accountId, groupInvitedNotification!!.groupId),
                        name = groupInvitedNotification.groupName,
                        ownerId = User.Id(
                            notification.accountId,
                            groupInvitedNotification.groupOwnerId
                        ),
                        createdAt = groupInvitedNotification.groupCreatedAt,
                        userIds = emptyList(),
                    ),
                    User.Id(notification.accountId, noteNotification!!.userId),
                    InvitationId(notification.accountId, groupInvitedNotification.invitationId),
                )
            }

            NotificationType.Mention -> {
                MentionNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    notification.createdAt,
                    User.Id(notification.accountId, noteNotification!!.userId),
                    Note.Id(notification.accountId, noteNotification.noteId),
                    isRead(),
                )
            }

            NotificationType.PollEnded -> {
                PollEndedNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    notification.createdAt,
                    isRead(),
                    Note.Id(notification.accountId, pollEndedNotification!!.noteId),
                )
            }

            NotificationType.PollVote -> {
                PollVoteNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    Note.Id(notification.accountId, noteNotification!!.noteId),
                    notification.createdAt,
                    User.Id(notification.accountId, noteNotification.userId),
                    pollVoteNotification!!.choice,
                    isRead()
                )
            }

            NotificationType.Post -> {
                PostNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    notification.createdAt,
                    User.Id(notification.accountId, noteNotification!!.userId),
                    Note.Id(notification.accountId, noteNotification.noteId),
                    isRead()
                )
            }

            NotificationType.Quote -> {
                QuoteNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    notification.createdAt,
                    User.Id(notification.accountId, noteNotification!!.userId),
                    Note.Id(notification.accountId, noteNotification.noteId),
                    isRead()
                )
            }

            NotificationType.Reaction -> {
                ReactionNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    notification.createdAt,
                    User.Id(notification.accountId, noteNotification!!.userId),
                    Note.Id(notification.accountId, noteNotification.noteId),
                    reactionNotification!!.reaction,
                    isRead()
                )
            }

            NotificationType.ReceiveFollowRequest -> {
                ReceiveFollowRequestNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    notification.createdAt,
                    User.Id(notification.accountId, noteNotification!!.userId),
                    isRead()
                )
            }

            NotificationType.Renote -> {
                RenoteNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    notification.createdAt,
                    User.Id(notification.accountId, noteNotification!!.userId),
                    Note.Id(notification.accountId, noteNotification.noteId),
                    isRead()
                )
            }

            NotificationType.Unknown -> {
                UnknownNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    notification.createdAt,
                    isRead(),
                    userId = User.Id(notification.accountId, noteNotification!!.userId),
                    rawType = unknownNotification!!.rawType,
                )
            }

            NotificationType.Reply -> {
                ReplyNotification(
                    Notification.Id(notification.accountId, notification.notificationId),
                    notification.createdAt,
                    User.Id(notification.accountId, noteNotification!!.userId),
                    Note.Id(notification.accountId, noteNotification.noteId),
                    isRead()
                )
            }
        }
    }

    private fun isRead(): Boolean {
        return unreadNotification?.none {
            it.accountId == notification.accountId
        } ?: true
    }
}

enum class NotificationType(
    val value: String
) {
    Favorite("favorite"),
    Follow("follow"),
    FollowRequestAccepted("follow_request_accepted"),
    GroupInvited("group_invited"),
    Mention("mention"),
    PollEnded("poll_ended"),
    PollVote("poll_vote"),
    Post("post"),
    Quote("quote"),
    Reaction("reaction"),
    ReceiveFollowRequest("receive_follow_request"),
    Renote("renote"),
    Reply("reply"),
    Unknown("unknown");

    companion object {
        fun modelOf(model: Notification): NotificationType {
            when (model) {
                is FavoriteNotification -> return Favorite
                is FollowNotification -> return Follow
                is FollowRequestAcceptedNotification -> return FollowRequestAccepted
                is GroupInvitedNotification -> return GroupInvited
                is MentionNotification -> return Mention
                is PollEndedNotification -> return PollEnded
                is PollVoteNotification -> return PollVote
                is PostNotification -> return Post
                is QuoteNotification -> return Quote
                is ReactionNotification -> return Reaction
                is ReceiveFollowRequestNotification -> return ReceiveFollowRequest
                is RenoteNotification -> return Renote
                is ReplyNotification -> return Reply
                is UnknownNotification -> return Unknown
            }
        }
    }
}