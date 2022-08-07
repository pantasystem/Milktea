package net.pantasystem.milktea.data.infrastructure.group

import androidx.room.*
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.user.User

@Entity(
    tableName = "group_v1",
    foreignKeys = [
        ForeignKey(
            parentColumns = ["accountId"],
            entity = Account::class,
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("accountId"),
        Index("serverId"),
        Index("accountId", "serverId", unique = true)
    ]
)
data class GroupRecord(
    val serverId: String,
    val accountId: Long,
    val createdAt: Instant,
    val name: String,
    val ownerId: String,
    @PrimaryKey(autoGenerate = true) val id: Long
) {
    companion object {
        fun from(model: Group): GroupRecord {
            return GroupRecord(
                model.id.groupId,
                model.id.accountId,
                model.createdAt,
                model.name,
                model.ownerId.id,
                id = 0L
            )
        }
    }
}

@Entity(
    tableName = "group_member_v1",
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["groupId"],
            entity = GroupRecord::class,
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index("groupId"),
        Index("groupId", "userId", unique = true)
    ]
)
data class GroupMemberIdRecord(
    val groupId: Long,
    val userId: String,
    @PrimaryKey(autoGenerate = true) val id: Long,
)

data class GroupRelatedRecord(
    @Embedded val group: GroupRecord,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId",
        entity = GroupMemberIdRecord::class
    )
    val userIds: List<GroupMemberIdRecord>,
) {
    fun toModel(): Group {
        return Group(
            id = Group.Id(accountId = group.accountId, group.serverId),
            createdAt = group.createdAt,
            name = group.name,
            ownerId = User.Id(
                group.accountId,
                group.ownerId,
            ),
            userIds = userIds.map {
                User.Id(group.accountId, it.userId)
            }
        )
    }
}