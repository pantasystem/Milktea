package net.pantasystem.milktea.api.misskey.v11

import net.pantasystem.milktea.api.misskey.I
import net.pantasystem.milktea.api.misskey.groups.*
import net.pantasystem.milktea.api.misskey.users.FollowFollowerUser
import net.pantasystem.milktea.api.misskey.users.RequestUser
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPIV11Diff {

    @POST("api/users/followers")
    suspend fun followers(@Body userRequest: RequestUser) : Response<List<FollowFollowerUser>>

    @POST("api/users/following")
    suspend fun following(@Body userRequest: RequestUser) : Response<List<FollowFollowerUser>>

    @POST("api/users/groups/create")
    suspend fun createGroup(@Body body: CreateGroupDTO) : Response<GroupDTO>

    @POST("api/users/groups/delete")
    suspend fun deleteGroup(@Body body: DeleteGroupDTO) : Response<Unit>

    @POST("api/users/groups/invitations/accept")
    suspend fun acceptInvitation(@Body body: AcceptInvitationDTO) : Response<Unit>

    @POST("api/users/groups/invitations/reject")
    suspend fun rejectInvitation(@Body body: RejectInvitationDTO) : Response<Unit>

    @POST("api/users/groups/invite")
    suspend fun invite(@Body body: InviteUserDTO) : Response<Unit>

    @POST("api/users/groups/joined")
    suspend fun joinedGroups(@Body body: I) : Response<List<GroupDTO>>

    @POST("api/users/groups/owned")
    suspend fun ownedGroups(@Body body: I) : Response<List<GroupDTO>>

    @POST("api/users/groups/owned")
    suspend fun pullUser(@Body body: RemoveUserDTO) : Response<Unit>

    @POST("api/users/groups/show")
    suspend fun showGroup(@Body body: ShowGroupDTO) : Response<GroupDTO>

    @POST("api/users/groups/transfer")
    suspend fun transferGroup(@Body body: TransferGroupDTO) : Response<GroupDTO>

    @POST("api/users/groups/update")
    suspend fun updateGroup(@Body body: UpdateGroupDTO) : Response<GroupDTO>
}