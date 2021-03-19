package jp.panta.misskeyandroidclient.model.list

import jp.panta.misskeyandroidclient.model.Entity
import jp.panta.misskeyandroidclient.model.EntityId
import jp.panta.misskeyandroidclient.model.users.User
import java.util.*

data class UserList(
    val id: Id,
    val createdAt: Date,
    val name: String,
    val userIds: List<User.Id>
) : Entity {

    data class Id(
        val accountId: Long,
        val userListId: String
    ) : EntityId
}