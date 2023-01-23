package net.pantasystem.milktea.data.infrastructure.user

import androidx.room.*
import net.pantasystem.milktea.model.user.nickname.UserNickname

@Entity(
    tableName = "nicknames",
    indices = [Index(value = arrayOf("username", "host"), unique = true)]
)
data class UserNicknameDTO (
    val nickname: String,

    @ColumnInfo(name = "username")
    val userName: String,

    @ColumnInfo(name = "host")
    val host: String,
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
)

@Dao
abstract class UserNicknameDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun create(dto: UserNicknameDTO)

    @Update
    abstract suspend fun update(dto: UserNicknameDTO)

    @Query("select * from nicknames")
    abstract suspend fun findAll(): List<UserNicknameDTO>

    @Query("select * from nicknames where username=:username and host=:host")
    abstract suspend fun findByUserNameAndHost(username: String, host: String): UserNicknameDTO?

    @Query("select * from nicknames where id=:id")
    abstract suspend fun findOne(id: Long): UserNicknameDTO?

    @Query("delete from nicknames where username=:username and host=:host")
    abstract suspend fun delete(username: String, host: String)

}

fun UserNicknameDTO.toUserNickname(): UserNickname {
    return UserNickname(
        UserNickname.Id(
            userName = userName,
            host = host
        ),
        name = nickname
    )
}

