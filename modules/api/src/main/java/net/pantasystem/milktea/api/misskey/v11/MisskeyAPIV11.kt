package net.pantasystem.milktea.api.misskey.v11

import net.pantasystem.milktea.api.misskey.I
import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.api.misskey.groups.*
import net.pantasystem.milktea.api.misskey.users.FollowFollowerUser
import net.pantasystem.milktea.api.misskey.users.RequestUser
import retrofit2.Response

open class MisskeyAPIV11(private val misskeyAPI: MisskeyAPI, private val apiDiff: MisskeyAPIV11Diff): MisskeyAPI by misskeyAPI{


    open suspend fun followers(userRequest: RequestUser): Response<List<FollowFollowerUser>> = apiDiff.followers(userRequest)
    open suspend fun following(userRequest: RequestUser): Response<List<FollowFollowerUser>> = apiDiff.following(userRequest)


    open suspend fun createGroup( body: CreateGroupDTO) : Response<GroupDTO> = apiDiff.createGroup(body)
    open suspend fun deleteGroup( body: DeleteGroupDTO) : Response<Unit> = apiDiff.deleteGroup(body)
    open suspend fun acceptInvitation( body: AcceptInvitationDTO) : Response<Unit> = apiDiff.acceptInvitation(body)
    open suspend fun rejectInvitation( body: RejectInvitationDTO) : Response<Unit> = apiDiff.rejectInvitation(body)
    open suspend fun invite( body: InviteUserDTO) : Response<Unit> = apiDiff.invite(body)
    open suspend fun joinedGroups( body: I) : Response<List<GroupDTO>> = apiDiff.joinedGroups(body)
    open suspend fun ownedGroups( body: I) : Response<List<GroupDTO>> = apiDiff.ownedGroups(body)
    open suspend fun pullUser( body: RemoveUserDTO) : Response<Unit> = apiDiff.pullUser(body)
    open suspend fun showGroup( body: ShowGroupDTO) : Response<GroupDTO> = apiDiff.showGroup(body)
    open suspend fun transferGroup( body: TransferGroupDTO) : Response<GroupDTO> = apiDiff.transferGroup(body)
    open suspend fun updateGroup( body: UpdateGroupDTO) : Response<GroupDTO> = apiDiff.updateGroup(body)

}