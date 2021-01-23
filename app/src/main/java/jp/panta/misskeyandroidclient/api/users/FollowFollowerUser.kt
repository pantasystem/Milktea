package jp.panta.misskeyandroidclient.api.users

import java.util.*

data class FollowFollowerUser(
    val id: String,
    val createdAt: Date,
    val followeeId: String,
    val followee: User?,
    val followerId: String,
    val follower: User?
)