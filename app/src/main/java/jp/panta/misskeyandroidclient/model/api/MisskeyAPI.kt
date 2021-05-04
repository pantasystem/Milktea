package jp.panta.misskeyandroidclient.model.api

import jp.panta.misskeyandroidclient.api.list.*
import jp.panta.misskeyandroidclient.api.notes.*
import jp.panta.misskeyandroidclient.api.notification.NotificationDTO
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.api.app.CreateApp
import jp.panta.misskeyandroidclient.api.app.ShowApp
import jp.panta.misskeyandroidclient.api.drive.*
import jp.panta.misskeyandroidclient.model.auth.signin.SignIn
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
import jp.panta.misskeyandroidclient.api.notes.reaction.ReactionHistoryDTO
import jp.panta.misskeyandroidclient.api.notes.reaction.RequestReactionHistoryDTO
import jp.panta.misskeyandroidclient.api.users.AcceptFollowRequest
import jp.panta.misskeyandroidclient.api.users.RejectFollowRequest
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.model.drive.Directory
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPI {

    @POST("api/signin")
    suspend fun signIn(@Body signIn: SignIn): Response<I>

    @POST("api/app/create")
    suspend fun createApp(@Body createApp: CreateApp): Response<App>

    @POST("api/my/apps")
    suspend fun myApps(@Body i: I) : Response<List<App>>

    @POST("api/app/show")
    suspend fun showApp(@Body showApp: ShowApp) : Response<App>

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
    suspend fun unFollowUser(@Body requestUser: RequestUser): Response<UserDTO>

    @POST("api/following/create")
    suspend fun followUser(@Body requestUser: RequestUser): Response<UserDTO>

    @POST("api/following/requests/accept")
    suspend fun acceptFollowRequest(@Body followRequest: AcceptFollowRequest) : Response<Unit>

    @POST("api/following/requests/reject")
    suspend fun rejectFollowRequest(@Body rejectFollowRequest: RejectFollowRequest) : Response<Unit>
    //account
    @POST("api/i/favorites")
    suspend fun favorites(@Body noteRequest: NoteRequest): Response<List<Favorite>?>

    @POST("api/notes/favorites/create")
    suspend fun createFavorite(@Body noteRequest: NoteRequest): Response<Unit>

    @POST("api/notes/favorites/delete")
    suspend fun deleteFavorite(@Body noteRequest: NoteRequest): Response<Unit>

    @POST("api/i/notifications")
    suspend fun notification(@Body notificationRequest: NotificationRequest): Response<List<NotificationDTO>?>

    @POST("api/notes/create")
    suspend fun create(@Body createNote: CreateNote): Response<CreateNote.Response>

    @POST("api/notes/delete")
    suspend fun delete(@Body deleteNote: DeleteNote): Response<Unit>

    @POST("api/notes/reactions")
    suspend fun reactions(@Body body: RequestReactionHistoryDTO): Response<List<ReactionHistoryDTO>>

    @POST("api/notes/reactions/create")
    suspend fun createReaction(@Body reaction: CreateReaction): Response<Unit>
    @POST("api/notes/reactions/delete")
    suspend fun deleteReaction(@Body deleteNote: DeleteNote): Response<Unit>

    @POST("api/notes/unrenote")
    suspend fun unrenote(@Body deleteNote: DeleteNote): Response<Unit>

    @POST("api/notes/search")
    suspend fun searchNote(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/state")
    suspend fun noteState(@Body noteRequest: NoteRequest): Response<State>

    @POST("api/notes/show")
    suspend fun showNote(@Body requestNote: NoteRequest): Response<NoteDTO>

    @POST("api/notes/children")
    suspend fun children(@Body noteRequest: NoteRequest): Response<List<NoteDTO>>

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



    //drive
    @POST("api/drive/files")
    suspend fun getFiles(@Body fileRequest: RequestFile): Response<List<FilePropertyDTO>>

    @POST("api/drive/folders")
    suspend fun getFolders(@Body folderRequest: RequestFolder): Response<List<Directory>>

    @POST("api/drive/folders/create")
    suspend fun createFolder(@Body createFolder: CreateFolder): Response<Unit>


    //meta
    @POST("api/meta")
    suspend fun getMeta(@Body requestMeta: RequestMeta): Response<Meta>


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
    suspend fun muteUser(@Body requestUser: RequestUser): Response<Unit>

    @POST("api/mute/delete")
    suspend fun unmuteUser(@Body requestUser: RequestUser): Response<Unit>

    @POST("api/hashtags/list")
    suspend fun getHashTagList(@Body requestHashTagList: RequestHashTagList): Response<List<HashTag>>

}