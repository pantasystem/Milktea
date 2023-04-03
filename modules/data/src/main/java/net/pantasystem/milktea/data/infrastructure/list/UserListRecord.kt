package net.pantasystem.milktea.data.infrastructure.list

import androidx.room.*
import kotlinx.datetime.Instant
import net.pantasystem.milktea.data.infrastructure.account.db.AccountRecord
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.user.User

@Entity(
    tableName = "user_list",
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
data class UserListRecord(
    @ColumnInfo(name = "serverId")
    val serverId: String,

    @ColumnInfo(name = "accountId")
    val accountId: Long,

    @ColumnInfo(name = "createdAt")
    val createdAt: Instant,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
)

@Entity(
    tableName = "user_list_member",
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["userListId"],
            entity = UserListRecord::class,
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index("userListId"),
        Index("userListId", "userId", unique = true)
    ]
)
data class UserListMemberIdRecord(
    @ColumnInfo(name = "userListId")
    val userListId: Long,

    @ColumnInfo(name = "userId")
    val userId: String,

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
)

@DatabaseView(
    """
        select m.userListId, u.id as userId, u.avatarUrl, u.serverId from user_list_member as m 
            inner join user_list as ul
            inner join user as u
            on m.userListId = ul.id
                and m.userId = u.serverId
                and ul.accountId = u.accountId
    """,
    viewName = "user_list_member_view"
)
data class UserListMemberView(
    val userListId: Long,
    val avatarUrl: String?,
    val userId: Long,
    val serverId: String,
)


data class UserListRelatedRecord(
    @Embedded val userList: UserListRecord,
    @Relation(
        parentColumn = "id",
        entityColumn = "userListId",
        entity = UserListMemberIdRecord::class
    )
    val userIds: List<UserListMemberIdRecord>,

    @Relation(
        parentColumn = "id",
        entityColumn = "userListId",
        entity = UserListMemberView::class
    )
    val members: List<UserListMemberView>,
) {

    fun toModel(): UserList {
        return UserList(
            id = UserList.Id(accountId = userList.accountId, userList.serverId),
            createdAt = userList.createdAt,
            name = userList.name,
            userIds = userIds.map {
                User.Id(userList.accountId, it.userId)
            }
        )
    }
}