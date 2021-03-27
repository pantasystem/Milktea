package jp.panta.misskeyandroidclient.api.v11

import jp.panta.misskeyandroidclient.api.groups.*
import jp.panta.misskeyandroidclient.api.users.FollowFollowerUser
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.model.I
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPIV11Diff {

    @POST("api/users/followers")
    fun followers(@Body userRequest: RequestUser) : Call<List<FollowFollowerUser>>

    @POST("api/users/following")
    fun following(@Body userRequest: RequestUser) : Call<List<FollowFollowerUser>>

    @POST("api/users/groups/create")
    fun createGroup(@Body body: CreateGroup) : Call<GroupDTO>

    @POST("api/users/groups/delete")
    fun deleteGroup(@Body body: DeleteGroup) : Call<Unit>

    @POST("api/users/invitations/accept")
    fun acceptInvitation(@Body body: AcceptInvitation) : Call<Unit>

    @POST("api/users/invitations/reject")
    fun rejectInvitation(@Body body: RejectInvitation) : Call<Unit>

    @POST("api/users/groups/invite")
    fun invite(@Body body: InviteUser) : Call<Unit>

    @POST("api/users/groups/joined")
    fun joinedGroups(@Body body: I) : Call<List<GroupDTO>>

    @POST("api/users/groups/owned")
    fun ownedGroups(@Body body: I) : Call<List<GroupDTO>>

    @POST("api/users/groups/owned")
    fun pullUser(@Body body: RemoveUser) : Call<Unit>

    @POST("api/users/groups/show")
    fun showGroup(@Body body: ShowGroup) : Call<Unit>

    @POST("api/users/groups/transfer")
    fun transferGroup(@Body body: TransferGroup) : Call<GroupDTO>

    @POST("api/users/groups/update")
    fun updateGroup(@Body body: UpdateGroup) : Call<GroupDTO>
}