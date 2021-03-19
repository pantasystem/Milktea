package jp.panta.misskeyandroidclient.api.list

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.serializations.DateSerializer
import java.io.Serializable
import java.util.Date

@kotlinx.serialization.Serializable
data class UserListDTO(
    val id: String,
    @kotlinx.serialization.Serializable(with = DateSerializer::class) val createdAt: Date,
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