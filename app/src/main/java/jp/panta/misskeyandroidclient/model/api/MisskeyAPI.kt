package jp.panta.misskeyandroidclient.model.api

import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.auth.AccessToken
import jp.panta.misskeyandroidclient.model.auth.AppSecret
import jp.panta.misskeyandroidclient.model.auth.Session
import jp.panta.misskeyandroidclient.model.auth.UserKey
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.FolderProperty
import jp.panta.misskeyandroidclient.model.drive.RequestFile
import jp.panta.misskeyandroidclient.model.drive.RequestFolder
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
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

    @POST("api/i/notifications")
    fun notification(@Body notificationRequest: NotificationRequest): Call<List<Notification>?>

    @POST("api/notes/create")
    fun create(@Body createNote: CreateNote): Call<Note?>

    @POST("api/notes/delete")
    fun delete(@Body deleteNote: DeleteNote)

    @POST("api/notes/reactions/create")
    fun createReaction(@Body reaction: CreateReaction): Call<Unit>
    @POST("api/notes/reactions/delete")
    fun deleteReaction(@Body deleteNote: DeleteNote): Call<Unit>

    @POST("api/notes/search")
    fun searchNote(@Body noteRequest: NoteRequest): Call<List<Note>?>

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


}