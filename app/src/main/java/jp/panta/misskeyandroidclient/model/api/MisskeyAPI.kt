package jp.panta.misskeyandroidclient.model.api

import jp.panta.misskeyandroidclient.model.auth.AccessToken
import jp.panta.misskeyandroidclient.model.auth.AppSecret
import jp.panta.misskeyandroidclient.model.auth.Session
import jp.panta.misskeyandroidclient.model.auth.UserKey
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.meta.RequestMeta
import jp.panta.misskeyandroidclient.model.notes.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPI {

    //auth
    @POST("api/auth/session/generate")
    fun generateSession(@Body appSecret: AppSecret): Call<Session>

    @POST("api/auth/userkey")
    fun getAccessToken(@Body userKey: UserKey): Call<AccessToken>

    @POST("api/notes/create")
    fun create(@Body createNote: CreateNote): Call<Note?>

    @POST("api/notes/delete")
    fun delete(@Body deleteNote: DeleteNote)

    @POST("api/notes/reactions/create")
    fun createReaction(@Body reaction: CreateReaction)
    @POST("api/notes/reactions/delete")
    fun deleteReaction(@Body deleteNote: DeleteNote)

    @POST("api/notes/search")
    fun searchNote(@Body noteRequest: NoteRequest): Call<List<Note>?>

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


    //meta
    @POST("api/meta")
    fun getMeta(@Body requestMeta: RequestMeta): Call<Meta>


}