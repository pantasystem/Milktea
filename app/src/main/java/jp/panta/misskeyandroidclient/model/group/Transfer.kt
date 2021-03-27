package jp.panta.misskeyandroidclient.model.group

import jp.panta.misskeyandroidclient.model.users.User

data class Transfer(
    val groupId: Group.Id,
    val userId: User.Id
)