package jp.panta.misskeyandroidclient.api.list

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
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