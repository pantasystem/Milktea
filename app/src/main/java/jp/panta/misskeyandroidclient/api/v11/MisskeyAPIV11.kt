package jp.panta.misskeyandroidclient.api.v11

import jp.panta.misskeyandroidclient.api.groups.*
import jp.panta.misskeyandroidclient.api.list.*
import jp.panta.misskeyandroidclient.api.notes.*
import jp.panta.misskeyandroidclient.api.notification.NotificationDTO
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.api.app.CreateApp
import jp.panta.misskeyandroidclient.api.app.ShowApp
import jp.panta.misskeyandroidclient.model.auth.signin.SignIn
import jp.panta.misskeyandroidclient.model.drive.*
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.model.hashtag.HashTag
import jp.panta.misskeyandroidclient.model.hashtag.RequestHashTagList
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.api.messaging.MessageAction
import jp.panta.misskeyandroidclient.api.messaging.RequestMessage
import jp.panta.misskeyandroidclient.model.messaging.RequestMessageHistory
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.RequestMeta
import jp.panta.misskeyandroidclient.model.notes.poll.Vote
import jp.panta.misskeyandroidclient.api.notification.NotificationRequest
import jp.panta.misskeyandroidclient.api.reaction.ReactionHistoryDTO
import jp.panta.misskeyandroidclient.api.reaction.RequestReactionHistoryDTO
import jp.panta.misskeyandroidclient.api.users.FollowFollowerUser
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import retrofit2.Call

open class MisskeyAPIV11(private val misskeyAPI: MisskeyAPI, private val apiDiff: MisskeyAPIV11Diff): MisskeyAPI by misskeyAPI{


    
    open fun createGroup( body: CreateGroupDTO) : Call<GroupDTO> = apiDiff.createGroup(body)
    open fun deleteGroup( body: DeleteGroupDTO) : Call<Unit> = apiDiff.deleteGroup(body)
    open fun acceptInvitation( body: AcceptInvitationDTO) : Call<Unit> = apiDiff.acceptInvitation(body)
    open fun rejectInvitation( body: RejectInvitationDTO) : Call<Unit> = apiDiff.rejectInvitation(body)
    open fun invite( body: InviteUserDTO) : Call<Unit> = apiDiff.invite(body)
    open fun joinedGroups( body: I) : Call<List<GroupDTO>> = apiDiff.joinedGroups(body)
    open fun ownedGroups( body: I) : Call<List<GroupDTO>> = apiDiff.ownedGroups(body)
    open fun pullUser( body: RemoveUserDTO) : Call<Unit> = apiDiff.pullUser(body)
    open fun showGroup( body: ShowGroupDTO) : Call<GroupDTO> = apiDiff.showGroup(body)
    open fun transferGroup( body: TransferGroupDTO) : Call<GroupDTO> = apiDiff.transferGroup(body)
    open fun updateGroup( body: UpdateGroupDTO) : Call<GroupDTO> = apiDiff.updateGroup(body)
    override fun reactions(body: RequestReactionHistoryDTO): Call<List<ReactionHistoryDTO>> = misskeyAPI.reactions(body)

}