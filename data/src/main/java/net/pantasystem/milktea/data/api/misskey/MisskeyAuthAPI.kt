package net.pantasystem.milktea.data.api.misskey

import net.pantasystem.milktea.api.misskey.auth.AccessToken
import net.pantasystem.milktea.api.misskey.auth.AppSecret
import net.pantasystem.milktea.api.misskey.auth.Session
import net.pantasystem.milktea.api.misskey.auth.UserKey
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAuthAPI {
    //auth
    @POST("api/auth/session/generate")
    suspend fun generateSession(@Body appSecret: net.pantasystem.milktea.api.misskey.auth.AppSecret): Response<net.pantasystem.milktea.api.misskey.auth.Session>

    @POST("api/auth/session/userkey")
    suspend fun getAccessToken(@Body userKey: net.pantasystem.milktea.api.misskey.auth.UserKey): Response<net.pantasystem.milktea.api.misskey.auth.AccessToken>
}