package net.pantasystem.milktea.data.infrastructure.group

import androidx.room.*
import kotlinx.datetime.Instant
import net.pantasystem.milktea.data.infrastructure.account.db.AccountRecord
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.user.User

@Entity(
    tableName = "group_v1",
    foreignKeys = [
        ForeignKey(
            parentColumns = ["accountId"],
            entity = AccountRecord::class,
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
    @ColumnInfo(name = "serverId")
    val serverId: String,

    @ColumnInfo(name = "accountId")
    val accountId: Long,

    @ColumnInfo(name = "createdAt")
    val createdAt: Instant,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "ownerId")
    val ownerId: String,

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long
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
    @ColumnInfo(name = "groupId")
    val groupId: Long,

    @ColumnInfo(name = "userId")
    val userId: String,

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long,
)

@DatabaseView(
    """
        select m.groupId, u.id as userId, u.avatarUrl, u.serverId from group_member_v1 as m 
            inner join group_v1 as g
            inner join user as u
            on m.groupId = g.id
                and m.userId = u.serverId
                and g.accountId = u.accountId
    """,
    viewName = "group_member_view"
)
data class GroupMemberView(
    @ColumnInfo(name = "groupId")
    val groupId: Long,

    @ColumnInfo(name = "avatarUrl")
    val avatarUrl: String?,

    @ColumnInfo(name = "userId")
    val userId: Long,

    @ColumnInfo(name = "serverId")
    val serverId: String,
)

data class GroupRelatedRecord(
    @Embedded val group: GroupRecord,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId",
        entity = GroupMemberIdRecord::class
    )
    val userIds: List<GroupMemberIdRecord>,

    @Relation(
        parentColumn = "id",
        entityColumn = "groupId",
        entity = GroupMemberView::class
    )
    val members: List<GroupMemberView>,
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