package jp.panta.misskeyandroidclient.api.v10

import jp.panta.misskeyandroidclient.api.users.UserDTO

data class FollowFollowerUsers(
    val users: List<UserDTO>,
    val next: String
)