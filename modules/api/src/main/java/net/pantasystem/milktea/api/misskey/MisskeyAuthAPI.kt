package net.pantasystem.milktea.api.misskey

import net.pantasystem.milktea.api.misskey.auth.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAuthAPI {
    //auth
    @POST("api/auth/session/generate")
    suspend fun generateSession(@Body appSecret: AppSecret): Response<Session>

    @POST("api/auth/session/userkey")
    suspend fun getAccessToken(@Body userKey: UserKey): Response<AccessToken>

    @POST("/api/signin")
    suspend fun signIn(@Body body: SignInRequest): Response<SignInResponse>
}