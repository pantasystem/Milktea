package jp.panta.misskeyandroidclient.model.group

import jp.panta.misskeyandroidclient.model.Entity
import jp.panta.misskeyandroidclient.model.EntityId
import jp.panta.misskeyandroidclient.model.users.User
import java.util.*

data class Group(
    val id: Id,
    val createdAt: Date,
    val name: String,
    val ownerId: User.Id,
    val userIds: List<User.Id>
) : Entity {
    data class Id(val accountId: Long, val groupId: String) : EntityId
}