package jp.panta.misskeyandroidclient.model.api

import jp.panta.misskeyandroidclient.api.notes.*
import jp.panta.misskeyandroidclient.api.notification.NotificationDTO
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.auth.custom.CreateApp
import jp.panta.misskeyandroidclient.model.auth.custom.ShowApp
import jp.panta.misskeyandroidclient.model.auth.signin.SignIn
import jp.panta.misskeyandroidclient.model.drive.*
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.model.hashtag.HashTag
import jp.panta.misskeyandroidclient.model.hashtag.RequestHashTagList
import jp.panta.misskeyandroidclient.model.list.*
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.model.messaging.MessageAction
import jp.panta.misskeyandroidclient.api.messaging.RequestMessage
import jp.panta.misskeyandroidclient.model.messaging.RequestMessageHistory
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.RequestMeta
import jp.panta.misskeyandroidclient.model.notes.poll.Vote
import jp.panta.misskeyandroidclient.api.notification.NotificationRequest
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPI {

    @POST("api/signin")
    fun signIn(@Body signIn: SignIn): Call<I>

    @POST("api/app/create")
    fun createApp(@Body createApp: CreateApp): Call<App>

    @POST("api/my/apps")
    fun myApps(@Body i: I) : Call<List<App>>

    @POST("api/app/show")
    fun showApp(@Body showApp: ShowApp) : Call<App>

    @POST("api/blocking/create")
    fun blockUser(@Body requestUser: RequestUser): Call<Unit>

    @POST("api/blocking/delete")
    fun unblockUser(@Body requestUser: RequestUser): Call<Unit>


    @POST("api/i")
    fun i(@Body i: I): Call<UserDTO>

    //users
    @POST("api/users")
    fun getUsers(@Body requestUser: RequestUser): Call<List<UserDTO>>

    @POST("api/users/show")
    fun showUser(@Body requestUser: RequestUser): Call<UserDTO>

    @POST("api/users/search")
    fun searchUser(@Body requestUser: RequestUser): Call<List<UserDTO>>

    @POST("api/users/lists/list")
    fun userList(@Body i: I): Call<List<UserList>>

    @POST("api/users/lists/show")
    fun showList(@Body listId: ListId): Call<UserList>

    @POST("api/users/lists/create")
    fun createList(@Body createList: CreateList): Call<UserList>

    @POST("api/lists/delete")
    fun deleteList(@Body listId: ListId): Call<Unit>

    @POST("api/users/lists/update")
    fun updateList(@Body createList: UpdateList): Call<Unit>

    @POST("api/users/lists/push")
    fun pushUserToList(@Body listUserOperation: ListUserOperation): Call<Unit>

    @POST("api/users/lists/pull")
    fun pullUserFromList(@Body listUserOperation: ListUserOperation): Call<Unit>

    @POST("api/following/delete")
    fun unFollowUser(@Body requestUser: RequestUser): Call<UserDTO>

    @POST("api/following/create")
    fun followUser(@Body requestUser: RequestUser): Call<UserDTO>

    //account
    @POST("api/i/favorites")
    fun favorites(@Body noteRequest: NoteRequest): Call<List<Favorite>?>

    @POST("api/notes/favorites/create")
    fun createFavorite(@Body noteRequest: NoteRequest): Call<Unit>

    @POST("api/notes/favorites/delete")
    fun deleteFavorite(@Body noteRequest: NoteRequest): Call<Unit>

    @POST("api/i/notifications")
    fun notification(@Body notificationRequest: NotificationRequest): Call<List<NotificationDTO>?>

    @POST("api/notes/create")
    fun create(@Body createNote: CreateNote): Call<CreateNote.Response>

    @POST("api/notes/delete")
    fun delete(@Body deleteNote: DeleteNote): Call<Unit>

    @POST("api/notes/reactions/create")
    fun createReaction(@Body reaction: CreateReaction): Call<Unit>
    @POST("api/notes/reactions/delete")
    fun deleteReaction(@Body deleteNote: DeleteNote): Call<Unit>

    @POST("api/notes/unrenote")
    fun unrenote(@Body deleteNote: DeleteNote): Call<Unit>

    @POST("api/notes/search")
    fun searchNote(@Body noteRequest: NoteRequest): Call<List<NoteDTO>?>

    @POST("api/notes/state")
    fun noteState(@Body noteRequest: NoteRequest): Call<State>

    @POST("api/notes/show")
    fun showNote(@Body requestNote: NoteRequest): Call<NoteDTO>

    @POST("api/notes/children")
    fun children(@Body noteRequest: NoteRequest): Call<List<NoteDTO>>

    @POST("api/notes/conversation")
    fun conversation(@Body noteRequest: NoteRequest): Call<List<NoteDTO>>

    @POST("api/notes/featured")
    fun featured(@Body noteRequest: NoteRequest): Call<List<NoteDTO>?>

    //timeline
    @POST("api/notes/timeline")
    fun homeTimeline(@Body noteRequest: NoteRequest): Call<List<NoteDTO>?>


    @POST("api/notes/hybrid-timeline")
    fun hybridTimeline(@Body noteRequest: NoteRequest): Call<List<NoteDTO>?>

    @POST("api/notes/local-timeline")
    fun localTimeline(@Body noteRequest: NoteRequest): Call<List<NoteDTO>?>

    @POST("api/notes/global-timeline")
    fun globalTimeline(@Body noteRequest: NoteRequest): Call<List<NoteDTO>?>

    @POST("api/notes/polls/vote")
    fun vote(@Body vote: Vote) : Call<Unit>

    @POST("api/notes/search-by-tag")
    fun searchByTag(@Body noteRequest: NoteRequest): Call<List<NoteDTO>?>

    @POST("api/notes/user-list-timeline")
    fun userListTimeline(@Body noteRequest: NoteRequest): Call<List<NoteDTO>?>
    //user
    @POST("api/users/notes")
    fun userNotes(@Body noteRequest: NoteRequest): Call<List<NoteDTO>?>

    @POST("api/notes/mentions")
    fun mentions(@Body noteRequest: NoteRequest): Call<List<NoteDTO>?>



    //drive
    @POST("api/drive/files")
    fun getFiles(@Body fileRequest: RequestFile): Call<List<FileProperty>>

    @POST("api/drive/folders")
    fun getFolders(@Body folderRequest: RequestFolder): Call<List<FolderProperty>>

    @POST("api/drive/folders/create")
    fun createFolder(@Body createFolder: CreateFolder): Call<Unit>


    //meta
    @POST("api/meta")
    fun getMeta(@Body requestMeta: RequestMeta): Call<Meta>


    //message
    @POST("api/messaging/history")
    fun getMessageHistory(@Body requestMessageHistory: RequestMessageHistory): Call<List<MessageDTO>>

    @POST("api/messaging/messages")
    fun getMessages(@Body requestMessage: RequestMessage): Call<List<MessageDTO>>

    @POST("api/messaging/messages/create")
    fun createMessage(@Body messageAction: MessageAction): Call<MessageDTO>

    @POST("api/messaging/messages/delete")
    fun deleteMessage(@Body messageAction: MessageAction): Call<Unit>

    @POST("api/messaging/messages/read")
    fun readMessage(@Body messageAction: MessageAction): Call<Unit>

    @POST("api/mute/create")
    fun muteUser(@Body requestUser: RequestUser): Call<Unit>

    @POST("api/mute/delete")
    fun unmuteUser(@Body requestUser: RequestUser): Call<Unit>

    @POST("api/hashtags/list")
    fun getHashTagList(@Body requestHashTagList: RequestHashTagList): Call<List<HashTag>>

}