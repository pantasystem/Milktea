package jp.panta.misskeyandroidclient.model.v10

import jp.panta.misskeyandroidclient.model.users.RequestUser
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPIV10Diff{

    @POST("api/users/followers")
    fun followers(@Body request: RequestFollowFollower): Call<FollowFollowerUsers>

    @POST("api/users/following")
    fun following(@Body request: RequestFollowFollower): Call<FollowFollowerUsers>
}