package jp.panta.misskeyandroidclient.api.users

import java.util.*

data class FollowFollowerUser(
    val id: String,
    val createdAt: Date,
    val followeeId: String,
    val followee: UserDTO?,
    val followerId: String,
    val follower: UserDTO?
)