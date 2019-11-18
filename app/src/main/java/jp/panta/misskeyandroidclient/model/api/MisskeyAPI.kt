package jp.panta.misskeyandroidclient.model.api

import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.FolderProperty
import jp.panta.misskeyandroidclient.model.drive.RequestFile
import jp.panta.misskeyandroidclient.model.drive.RequestFolder
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.RequestMessage
import jp.panta.misskeyandroidclient.model.messaging.RequestMessageHistory
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.meta.RequestMeta
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.notification.NotificationRequest
import jp.panta.misskeyandroidclient.model.users.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPI {


    @POST("api/i")
    fun i(@Body i: I): Call<User>

    //account
    @POST("api/i/favorites")
    fun favorites(@Body noteRequest: NoteRequest): Call<List<Favorite>?>

    @POST("api/notes/favorites/create")
    fun createFavorite(@Body noteRequest: NoteRequest): Call<Unit>

    @POST("api/notes/favorites/delete")
    fun deleteFavorite(@Body noteRequest: NoteRequest): Call<Unit>

    @POST("api/i/notifications")
    fun notification(@Body notificationRequest: NotificationRequest): Call<List<Notification>?>

    @POST("api/notes/create")
    fun create(@Body createNote: CreateNote): Call<CreateNote.Response>

    @POST("api/notes/delete")
    fun delete(@Body deleteNote: DeleteNote)

    @POST("api/notes/reactions/create")
    fun createReaction(@Body reaction: CreateReaction): Call<Unit>
    @POST("api/notes/reactions/delete")
    fun deleteReaction(@Body deleteNote: DeleteNote): Call<Unit>

    @POST("api/notes/search")
    fun searchNote(@Body noteRequest: NoteRequest): Call<List<Note>?>

    @POST("api/notes/state")
    fun noteState(@Body noteRequest: NoteRequest): Call<State>

    @POST("api/notes/show")
    fun showNote(@Body requestNote: NoteRequest): Call<Note>

    @POST("api/notes/children")
    fun children(@Body noteRequest: NoteRequest): Call<List<Note>>

    @POST("api/notes/conversation")
    fun conversation(@Body noteRequest: NoteRequest): Call<List<Note>>

    @POST("api/notes/featured")
    fun featured(@Body noteRequest: NoteRequest): Call<List<Note>?>

    //timeline
    @POST("api/notes/timeline")
    fun homeTimeline(@Body noteRequest: NoteRequest): Call<List<Note>?>


    @POST("api/notes/hybrid-timeline")
    fun hybridTimeline(@Body noteRequest: NoteRequest): Call<List<Note>?>

    @POST("api/notes/local-timeline")
    fun localTimeline(@Body noteRequest: NoteRequest): Call<List<Note>?>

    @POST("api/notes/global-timeline")
    fun globalTimeline(@Body noteRequest: NoteRequest): Call<List<Note>?>

    @POST("api/notes/search_by_tag")
    fun searchByTag(@Body noteRequest: NoteRequest): Call<List<Note>?>

    //user
    @POST("api/users/notes")
    fun userNotes(@Body noteRequest: NoteRequest): Call<List<Note>?>


    //drive
    @POST("api/drive/files")
    fun getFiles(@Body fileRequest: RequestFile): Call<List<FileProperty>>

    @POST("api/drive/folders")
    fun getFolders(@Body folderRequest: RequestFolder): Call<List<FolderProperty>>


    //meta
    @POST("api/meta")
    fun getMeta(@Body requestMeta: RequestMeta): Call<Meta>


    //message
    @POST("api/messaging/history")
    fun getMessageHistory(@Body requestMessageHistory: RequestMessageHistory): Call<List<Message>>

    @POST("api/messaging/messages")
    fun getMessages(@Body requestMessage: RequestMessage): Call<List<Message>>
}