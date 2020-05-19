package jp.panta.misskeyandroidclient.model.v10

import jp.panta.misskeyandroidclient.model.users.User

data class FollowFollowerUsers(
    val users: List<User>,
    val next: String
)