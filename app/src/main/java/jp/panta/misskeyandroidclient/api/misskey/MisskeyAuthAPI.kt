package jp.panta.misskeyandroidclient.api.misskey

import jp.panta.misskeyandroidclient.api.misskey.auth.AccessToken
import jp.panta.misskeyandroidclient.api.misskey.auth.AppSecret
import jp.panta.misskeyandroidclient.api.misskey.auth.Session
import jp.panta.misskeyandroidclient.api.misskey.auth.UserKey
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAuthAPI {
    //auth
    @POST("api/auth/session/generate")
    suspend fun generateSession(@Body appSecret: AppSecret): Response<Session>

    @POST("api/auth/session/userkey")
    suspend fun getAccessToken(@Body userKey: UserKey): Response<AccessToken>
}