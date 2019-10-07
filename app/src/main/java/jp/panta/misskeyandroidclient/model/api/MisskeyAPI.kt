package jp.panta.misskeyandroidclient.model.api

import android.arch.lifecycle.LiveData
import jp.panta.misskeyandroidclient.model.auth.AccessToken
import jp.panta.misskeyandroidclient.model.auth.AppSecret
import jp.panta.misskeyandroidclient.model.auth.Session
import jp.panta.misskeyandroidclient.model.auth.UserKey
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.TimelineRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPI {

    //auth
    @POST("api/auth/session/generate")
    fun generateSession(@Body appSecret: AppSecret): Call<Session>

    @POST("api/auth/userkey")
    fun getAccessToken(@Body userKey: UserKey): Call<AccessToken>

    //timeline
    @POST("api/notes/timeline")
    fun homeTimeline(@Body timelineRequest: TimelineRequest): Call<List<Note>?>


    @POST("api/notes/hybrid-timeline")
    fun hybridTimeline(@Body timelineRequest: TimelineRequest): Call<List<Note>?>

    @POST("api/notes/local-timeline")
    fun localTimeline(@Body timelineRequest: TimelineRequest): Call<List<Note>?>

    @POST("api/notes/global-timeline")
    fun globalTimeline(@Body timelineRequest: TimelineRequest): Call<List<Note>?>




}