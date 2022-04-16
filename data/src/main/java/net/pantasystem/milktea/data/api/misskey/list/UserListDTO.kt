package net.pantasystem.milktea.data.api.misskey.list


import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.list.UserList
import net.pantasystem.milktea.data.model.users.User
import java.io.Serializable

@kotlinx.serialization.Serializable
data class UserListDTO(
    val id: String,
    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,
    val name: String,
    val userIds: List<String>
) : Serializable {

    fun toEntity(account: Account): UserList {
        return UserList(
            UserList.Id(account.accountId, id),
            createdAt,
            name,
            userIds.map {
                User.Id(account.accountId, it)
            }
        )
    }
}