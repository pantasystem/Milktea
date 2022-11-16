package net.pantasystem.milktea.api.misskey.v10

import net.pantasystem.milktea.api.misskey.MisskeyAPI
import retrofit2.Response

open class MisskeyAPIV10(val misskey: MisskeyAPI, private val diff: MisskeyAPIV10Diff) :
    MisskeyAPI by misskey {

    open suspend fun following(followFollower: RequestFollowFollower): Response<FollowFollowerUsers> = diff.following(followFollower)
    open suspend fun followers(followFollower: RequestFollowFollower): Response<FollowFollowerUsers> = diff.followers(followFollower)

}