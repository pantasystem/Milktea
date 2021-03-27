package jp.panta.misskeyandroidclient.model.group

import jp.panta.misskeyandroidclient.model.users.User

data class Pull (
    val groupId: Group.Id,
    val userId: User.Id,
)