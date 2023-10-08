package net.pantasystem.milktea.api.misskey

import kotlinx.serialization.json.JsonObject
import net.pantasystem.milktea.api.misskey.ap.ApResolveRequest
import net.pantasystem.milktea.api.misskey.ap.ApResolveResult
import net.pantasystem.milktea.api.misskey.app.CreateApp
import net.pantasystem.milktea.api.misskey.auth.App
import net.pantasystem.milktea.api.misskey.clip.*
import net.pantasystem.milktea.api.misskey.drive.*
import net.pantasystem.milktea.api.misskey.favorite.Favorite
import net.pantasystem.milktea.api.misskey.groups.AcceptInvitationDTO
import net.pantasystem.milktea.api.misskey.groups.CreateGroupDTO
import net.pantasystem.milktea.api.misskey.groups.DeleteGroupDTO
import net.pantasystem.milktea.api.misskey.groups.GroupDTO
import net.pantasystem.milktea.api.misskey.groups.InviteUserDTO
import net.pantasystem.milktea.api.misskey.groups.RejectInvitationDTO
import net.pantasystem.milktea.api.misskey.groups.RemoveUserDTO
import net.pantasystem.milktea.api.misskey.groups.ShowGroupDTO
import net.pantasystem.milktea.api.misskey.groups.TransferGroupDTO
import net.pantasystem.milktea.api.misskey.groups.UpdateGroupDTO
import net.pantasystem.milktea.api.misskey.hashtag.SearchHashtagRequest
import net.pantasystem.milktea.api.misskey.instance.MetaNetworkDTO
import net.pantasystem.milktea.api.misskey.instance.RequestMeta
import net.pantasystem.milktea.api.misskey.list.*
import net.pantasystem.milktea.api.misskey.messaging.MessageAction
import net.pantasystem.milktea.api.misskey.messaging.MessageDTO
import net.pantasystem.milktea.api.misskey.messaging.RequestMessage
import net.pantasystem.milktea.api.misskey.messaging.RequestMessageHistory
import net.pantasystem.milktea.api.misskey.notes.*
import net.pantasystem.milktea.api.misskey.notes.favorite.CreateFavorite
import net.pantasystem.milktea.api.misskey.notes.favorite.DeleteFavorite
import net.pantasystem.milktea.api.misskey.notes.mute.ToggleThreadMuteRequest
import net.pantasystem.milktea.api.misskey.notes.reaction.ReactionHistoryDTO
import net.pantasystem.milktea.api.misskey.notes.reaction.RequestReactionHistoryDTO
import net.pantasystem.milktea.api.misskey.notes.translation.Translate
import net.pantasystem.milktea.api.misskey.notes.translation.TranslationResult
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationRequest
import net.pantasystem.milktea.api.misskey.online.user.OnlineUserCount
import net.pantasystem.milktea.api.misskey.register.Subscription
import net.pantasystem.milktea.api.misskey.register.SubscriptionStateNetworkDTO
import net.pantasystem.milktea.api.misskey.register.UnSubscription
import net.pantasystem.milktea.api.misskey.register.WebClientBaseRequest
import net.pantasystem.milktea.api.misskey.register.WebClientRegistries
import net.pantasystem.milktea.api.misskey.trend.HashtagTrend
import net.pantasystem.milktea.api.misskey.users.*
import net.pantasystem.milktea.api.misskey.users.follow.FollowUserRequest
import net.pantasystem.milktea.api.misskey.users.follow.UnFollowUserRequest
import net.pantasystem.milktea.api.misskey.users.follow.UpdateUserFollowRequest
import net.pantasystem.milktea.api.misskey.users.renote.mute.CreateRenoteMuteRequest
import net.pantasystem.milktea.api.misskey.users.renote.mute.DeleteRenoteMuteRequest
import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMuteDTO
import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMutesRequest
import net.pantasystem.milktea.api.misskey.users.report.ReportDTO
import net.pantasystem.milktea.api.misskey.v10.FollowFollowerUsers
import net.pantasystem.milktea.api.misskey.v10.RequestFollowFollower
import net.pantasystem.milktea.api.misskey.v12.antenna.AntennaDTO
import net.pantasystem.milktea.api.misskey.v12.antenna.AntennaQuery
import net.pantasystem.milktea.api.misskey.v12.antenna.AntennaToAdd
import net.pantasystem.milktea.api.misskey.v12.channel.ChannelDTO
import net.pantasystem.milktea.api.misskey.v12.channel.CreateChannelDTO
import net.pantasystem.milktea.api.misskey.v12.channel.FindPageable
import net.pantasystem.milktea.api.misskey.v12.channel.FollowChannelDTO
import net.pantasystem.milktea.api.misskey.v12.channel.ShowChannelDTO
import net.pantasystem.milktea.api.misskey.v12.channel.UnFollowChannelDTO
import net.pantasystem.milktea.api.misskey.v12.channel.UpdateChannelDTO
import net.pantasystem.milktea.api.misskey.v12.user.reaction.UserReaction
import net.pantasystem.milktea.api.misskey.v12.user.reaction.UserReactionRequest
import net.pantasystem.milktea.api.misskey.v12_75_0.CreateGallery
import net.pantasystem.milktea.api.misskey.v12_75_0.Delete
import net.pantasystem.milktea.api.misskey.v12_75_0.GalleryPost
import net.pantasystem.milktea.api.misskey.v12_75_0.GetPosts
import net.pantasystem.milktea.api.misskey.v12_75_0.Like
import net.pantasystem.milktea.api.misskey.v12_75_0.LikedGalleryPost
import net.pantasystem.milktea.api.misskey.v12_75_0.Show
import net.pantasystem.milktea.api.misskey.v12_75_0.UnLike
import net.pantasystem.milktea.api.misskey.v12_75_0.Update
import net.pantasystem.milktea.api.misskey.v13.EmojisResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MisskeyAPI {


    @POST("api/app/create")
    suspend fun createApp(@Body createApp: CreateApp): Response<App>


    @POST("api/blocking/create")
    suspend fun blockUser(@Body requestUser: RequestUser): Response<Unit>

    @POST("api/blocking/delete")
    suspend fun unblockUser(@Body requestUser: RequestUser): Response<Unit>


    @POST("api/i")
    suspend fun i(@Body i: I): Response<UserDTO>

    //users
    @POST("api/users")
    suspend fun getUsers(@Body requestUser: RequestUser): Response<List<UserDTO>>

    @POST("api/users/show")
    suspend fun showUser(@Body requestUser: RequestUser): Response<UserDTO>

    @POST("api/users/show")
    suspend fun showUsers(@Body requestUser: RequestUser): Response<List<UserDTO>>

    @POST("api/users/search")
    suspend fun searchUser(@Body requestUser: RequestUser): Response<List<UserDTO>>

    @POST("api/users/lists/list")
    suspend fun userList(@Body i: I): Response<List<UserListDTO>>

    @POST("api/users/lists/show")
    suspend fun showList(@Body listId: ListId): Response<UserListDTO>

    @POST("api/users/lists/create")
    suspend fun createList(@Body createList: CreateList): Response<UserListDTO>

    @POST("api/lists/delete")
    suspend fun deleteList(@Body listId: ListId): Response<Unit>

    @POST("api/users/lists/update")
    suspend fun updateList(@Body createList: UpdateList): Response<Unit>

    @POST("api/users/lists/push")
    suspend fun pushUserToList(@Body listUserOperation: ListUserOperation): Response<Unit>

    @POST("api/users/lists/pull")
    suspend fun pullUserFromList(@Body listUserOperation: ListUserOperation): Response<Unit>

    @POST("api/following/delete")
    suspend fun unFollowUser(@Body requestUser: UnFollowUserRequest): Response<UserDTO>

    @POST("api/following/create")
    suspend fun followUser(@Body requestUser: FollowUserRequest): Response<UserDTO>

    @POST("api/following/update")
    suspend fun updateFollowUser(@Body request: UpdateUserFollowRequest): Response<UserDTO>

    @POST("api/following/requests/accept")
    suspend fun acceptFollowRequest(@Body followRequest: AcceptFollowRequest) : Response<Unit>

    @POST("api/following/requests/reject")
    suspend fun rejectFollowRequest(@Body rejectFollowRequest: RejectFollowRequest) : Response<Unit>

    @POST("api/following/requests/list")
    suspend fun getFollowRequestsList(@Body body: GetFollowRequest): Response<List<FollowRequestDTO>>
    //account
    @POST("api/i/favorites")
    suspend fun favorites(@Body noteRequest: NoteRequest): Response<List<Favorite>?>

    @POST("api/notes/favorites/create")
    suspend fun createFavorite(@Body noteRequest: CreateFavorite): Response<Unit>

    @POST("api/notes/favorites/delete")
    suspend fun deleteFavorite(@Body noteRequest: DeleteFavorite): Response<Unit>

    @POST("api/i/notifications")
    suspend fun notification(@Body notificationRequest: NotificationRequest): Response<List<NotificationDTO>?>

    @POST("api/notifications/mark-all-as-read")
    suspend fun markAllAsReadNotifications(i: I): Response<Unit>

    @POST("api/notes/create")
    suspend fun create(@Body createNote: CreateNote): Response<CreateNote.Response>

    @POST("api/notes/delete")
    suspend fun delete(@Body deleteNote: DeleteNote): Response<Unit>

    @POST("api/notes/reactions")
    suspend fun reactions(@Body body: RequestReactionHistoryDTO): Response<List<ReactionHistoryDTO>>

    @POST("api/notes/reactions/create")
    suspend fun createReaction(@Body reaction: CreateReactionDTO): Response<Unit>
    @POST("api/notes/reactions/delete")
    suspend fun deleteReaction(@Body deleteNote: DeleteNote): Response<Unit>

    @POST("api/notes/unrenote")
    suspend fun unrenote(@Body deleteNote: DeleteNote): Response<Unit>

    @POST("api/notes/search")
    suspend fun searchNote(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/state")
    suspend fun noteState(@Body noteRequest: NoteRequest): Response<NoteStateResponse>

    @POST("api/notes/show")
    suspend fun showNote(@Body requestNote: NoteRequest): Response<NoteDTO>

    @POST("api/notes/children")
    suspend fun children(@Body req: GetNoteChildrenRequest): Response<List<NoteDTO>>

    @POST("api/notes/conversation")
    suspend fun conversation(@Body noteRequest: NoteRequest): Response<List<NoteDTO>>

    @POST("api/notes/featured")
    suspend fun featured(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    //timeline
    @POST("api/notes/timeline")
    suspend fun homeTimeline(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>


    @POST("api/notes/hybrid-timeline")
    suspend fun hybridTimeline(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/local-timeline")
    suspend fun localTimeline(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/global-timeline")
    suspend fun globalTimeline(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/polls/vote")
    suspend fun vote(@Body vote: Vote) : Response<Unit>

    @POST("api/notes/search-by-tag")
    suspend fun searchByTag(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/user-list-timeline")
    suspend fun userListTimeline(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>
    //user
    @POST("api/users/notes")
    suspend fun userNotes(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/mentions")
    suspend fun mentions(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/recommended-timeline")
    suspend fun getCalckeyRecommendedTimeline(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    //drive
    @POST("api/drive/files")
    suspend fun getFiles(@Body fileRequest: RequestFile): Response<List<FilePropertyDTO>>

    @POST("api/drive/files/update")
    suspend fun updateFile(@Body updateFileRequest: JsonObject): Response<FilePropertyDTO>

    @POST("api/drive/files/delete")
    suspend fun deleteFile(@Body req: DeleteFileDTO): Response<Unit>

    @POST("api/drive/files/show")
    suspend fun showFile(@Body req: ShowFile) : Response<FilePropertyDTO>

    @POST("api/drive/folders")
    suspend fun getFolders(@Body folderRequest: RequestFolder): Response<List<DirectoryNetworkDTO>>

    @POST("api/drive/folders/create")
    suspend fun createFolder(@Body createFolder: CreateFolder): Response<DirectoryNetworkDTO>

    @POST("api/drive/folders/show")
    suspend fun showFolder(@Body req: ShowFolderRequest): Response<DirectoryNetworkDTO>


    //meta
    @POST("api/meta")
    suspend fun getMeta(@Body requestMeta: RequestMeta): Response<MetaNetworkDTO>


    //message
    @POST("api/messaging/history")
    suspend fun getMessageHistory(@Body requestMessageHistory: RequestMessageHistory): Response<List<MessageDTO>>

    @POST("api/messaging/messages")
    suspend fun getMessages(@Body requestMessage: RequestMessage): Response<List<MessageDTO>>

    @POST("api/messaging/messages/create")
    suspend fun createMessage(@Body messageAction: MessageAction): Response<MessageDTO>

    @POST("api/messaging/messages/delete")
    suspend fun deleteMessage(@Body messageAction: MessageAction): Response<Unit>

    @POST("api/messaging/messages/read")
    suspend fun readMessage(@Body messageAction: MessageAction): Response<Unit>

    @POST("api/mute/create")
    suspend fun muteUser(@Body createMuteRequest: CreateMuteUserRequest): Response<Unit>

    @POST("api/mute/delete")
    suspend fun unmuteUser(@Body requestUser: RequestUser): Response<Unit>


    @POST("api/sw/register")
    suspend fun swRegister(@Body subscription: Subscription) : Response<SubscriptionStateNetworkDTO>

    @POST("api/sw/unregister")
    suspend fun swUnRegister(@Body unSub: UnSubscription) : Response<Unit>

    @POST("api/following/requests/cancel")
    suspend fun cancelFollowRequest(@Body req: CancelFollow) : Response<UserDTO>

    @POST("api/notes/renotes")
    suspend fun renotes(@Body req: FindRenotes) : Response<List<NoteDTO>>

    @POST("api/notes/translate")
    @Headers("Content-Type: application/json")
    suspend fun translate(@Body req: Translate) : Response<TranslationResult>

    @POST("api/users/report-abuse")
    suspend fun report(@Body req: ReportDTO) : Response<Unit>

    @POST("api/ap/show")
    suspend fun resolve(@Body req: ApResolveRequest) : Response<ApResolveResult>

    @POST("api/notes/thread-muting/create")
    suspend fun createThreadMute(@Body req: ToggleThreadMuteRequest) : Response<Unit>

    @POST("api/notes/thread-muting/delete")
    suspend fun deleteThreadMute(@Body req: ToggleThreadMuteRequest) : Response<Unit>

    @POST("api/hashtags/search")
    suspend fun searchHashtag(@Body req: SearchHashtagRequest) : Response<List<String>>

    @POST("api/emojis")
    suspend fun getEmojis(@Body req: EmptyRequest) : Response<EmojisResponse>

    @POST("api/i/registry/get-all")
    suspend fun getReactionsFromGetAll(@Body req: WebClientBaseRequest): Response<WebClientRegistries>

    @POST("api/clips/create")
    suspend fun createClip(@Body req: CreateClipRequest): Response<ClipDTO>

    @POST("api/clips/update")
    suspend fun updateClip(@Body req: UpdateClipRequest): Response<ClipDTO>

    @POST("api/clips/delete")
    suspend fun deleteClip(@Body req: DeleteClipRequest): Response<Unit>

    @POST("api/users/clips")
    suspend fun findByUsersClip(@Body req: FindUsersClipRequest): Response<List<ClipDTO>>

    @POST("api/notes/clips")
    suspend fun findByNotesClip(@Body req: FindNotesClip): Response<List<ClipDTO>>


    @POST("api/clips/add-note")
    suspend fun addNoteToClip(@Body req: AddNoteToClipRequest): Response<Unit>

    @POST("api/clips/remove-note")
    suspend fun removeNoteToClip(@Body req: RemoveNoteToClipRequest): Response<Unit>

    @POST("api/clips/show")
    suspend fun showClip(@Body req: ShowClipRequest): Response<ClipDTO>

    @POST("api/clips/list")
    suspend fun findMyClips(@Body req: I): Response<List<ClipDTO>>

    @POST("api/clips/notes")
    suspend fun getClipNotes(@Body req: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/renote-mute/list")
    suspend fun getRenoteMutes(@Body req: RenoteMutesRequest): Response<List<RenoteMuteDTO>>

    @POST("api/renote-mute/create")
    suspend fun createRenoteMute(@Body req: CreateRenoteMuteRequest): Response<Unit>

    @POST("api/renote-mute/delete")
    suspend fun deleteRenoteMute(@Body req: DeleteRenoteMuteRequest): Response<Unit>

    @POST("api/hashtags/trend")
    suspend fun getTrendingHashtags(@Body body: EmptyRequest): Response<List<HashtagTrend>>

    @POST("api/get-online-users-count")
    suspend fun getOnlineUsersCount(@Body body: EmptyRequest): Response<OnlineUserCount>


    @POST("api/users/followers")
    suspend fun followers4V10(@Body request: RequestFollowFollower): Response<FollowFollowerUsers>

    @POST("api/users/following")
    suspend fun following4V10(@Body request: RequestFollowFollower): Response<FollowFollowerUsers>


    @POST("/api/gallery/featured")
    suspend fun featuredGalleries(@Body i: I) : Response<List<GalleryPost>>

    @POST("/api/gallery/popular")
    suspend fun popularGalleries(@Body i: I) : Response<List<GalleryPost>>

    @POST("/api/gallery/posts")
    suspend fun galleryPosts(@Body getGalleryPost: GetPosts) : Response<List<GalleryPost>>

    @POST("/api/gallery/posts/create")
    suspend fun createGallery(@Body createGallery: CreateGallery) : Response<GalleryPost>

    @POST("/api/gallery/posts/delete")
    suspend fun deleteGallery(@Body deleteGallery: Delete) : Response<Unit>

    @POST("/api/gallery/posts/like")
    suspend fun likeGallery(@Body like: Like) : Response<Unit>

    @POST("/api/gallery/posts/unlike")
    suspend fun unlikeGallery(@Body unlike: UnLike) : Response<Unit>

    @POST("/api/gallery/posts/show")
    suspend fun showGallery(@Body show: Show) : Response<GalleryPost>

    @POST("/api/gallery/posts/update")
    suspend fun updateGallery(@Body update: Update) : Response<GalleryPost>

    @POST("/api/i/gallery/posts")
    suspend fun myGalleryPosts(@Body request: GetPosts) : Response<List<GalleryPost>>

    @POST("/api/i/gallery/likes")
    suspend fun likedGalleryPosts(@Body request: GetPosts) : Response<List<LikedGalleryPost>>

    @POST("/api/users/gallery/posts")
    suspend fun userPosts(@Body request: GetPosts) : Response<List<GalleryPost>>


    @POST("api/antennas/create")
    suspend fun createAntenna(@Body antennaToAdd: AntennaToAdd): Response<AntennaDTO>

    @POST("api/antennas/delete")
    suspend fun deleteAntenna(@Body query: AntennaQuery): Response<Unit>

    @POST("api/antennas/notes")
    suspend fun antennasNotes(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/antennas/show")
    suspend fun showAntenna(@Body antennaQuery: AntennaQuery): Response<AntennaDTO>

    @POST("api/antennas/update")
    suspend fun updateAntenna(@Body antennaToAdd: AntennaToAdd): Response<AntennaDTO>

    @POST("api/antennas/list")
    suspend fun getAntennas(@Body query: AntennaQuery): Response<List<AntennaDTO>>

    @POST("api/users/search-by-username-and-host")
    suspend fun searchByUserNameAndHost(@Body requestUser: RequestUser): Response<List<UserDTO>>

    @POST("api/channels/create")
    suspend fun createChannel(@Body dto: CreateChannelDTO): Response<ChannelDTO>

    @POST("api/channels/featured")
    suspend fun featuredChannels(@Body i: I): Response<List<ChannelDTO>>

    @POST("api/channels/follow")
    suspend fun followChannel(@Body dto: FollowChannelDTO): Response<Unit>

    @POST("api/channels/unfollow")
    suspend fun unFollowChannel(@Body dto: UnFollowChannelDTO): Response<Unit>

    @POST("api/channels/followed")
    suspend fun followedChannels(@Body dto: FindPageable): Response<List<ChannelDTO>>

    @POST("api/channels/owned")
    suspend fun ownedChannels(@Body dto: FindPageable): Response<List<ChannelDTO>>

    @POST("api/channels/show")
    suspend fun showChannel(@Body dto: ShowChannelDTO): Response<ChannelDTO>

    @POST("api/channels/update")
    suspend fun updateChannel(@Body dto: UpdateChannelDTO): Response<ChannelDTO>

    @POST("api/channels/timeline")
    suspend fun channelTimeline(@Body dto: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/users/reactions")
    suspend fun getUserReactions(@Body request: UserReactionRequest): Response<List<UserReaction>>



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