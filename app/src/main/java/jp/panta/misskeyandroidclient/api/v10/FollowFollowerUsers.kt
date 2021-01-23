package jp.panta.misskeyandroidclient.api.v10

import jp.panta.misskeyandroidclient.api.users.User

data class FollowFollowerUsers(
    val users: List<User>,
    val next: String
)