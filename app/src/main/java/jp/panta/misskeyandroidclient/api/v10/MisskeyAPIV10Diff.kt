package jp.panta.misskeyandroidclient.api.v10

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPIV10Diff{

    @POST("api/users/followers")
    suspend fun followers(@Body request: RequestFollowFollower): Response<FollowFollowerUsers>

    @POST("api/users/following")
    suspend fun following(@Body request: RequestFollowFollower): Response<FollowFollowerUsers>
}