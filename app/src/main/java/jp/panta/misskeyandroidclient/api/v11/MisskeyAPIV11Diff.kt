package jp.panta.misskeyandroidclient.api.v11

import jp.panta.misskeyandroidclient.model.users.FollowFollowerUser
import jp.panta.misskeyandroidclient.model.users.RequestUser
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPIV11Diff {

    @POST("api/users/followers")
    fun followers(@Body userRequest: RequestUser) : Call<List<FollowFollowerUser>>

    @POST("api/users/following")
    fun following(@Body userRequest: RequestUser) : Call<List<FollowFollowerUser>>
}