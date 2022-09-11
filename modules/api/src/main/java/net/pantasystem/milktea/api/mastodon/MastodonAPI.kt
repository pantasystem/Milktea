package net.pantasystem.milktea.api.mastodon

import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.mastodon.apps.AccessToken
import net.pantasystem.milktea.api.mastodon.apps.App
import net.pantasystem.milktea.api.mastodon.apps.CreateApp
import net.pantasystem.milktea.api.mastodon.apps.ObtainToken
import net.pantasystem.milktea.api.mastodon.emojis.TootEmojiDTO
import net.pantasystem.milktea.api.mastodon.instance.Instance
import retrofit2.Response
import retrofit2.http.Body

import retrofit2.http.GET
import retrofit2.http.POST

interface MastodonAPI {

    @GET("api/v1/instance")
    suspend fun getInstance(): Instance

    @GET("api/v1/custom_emojis")
    suspend fun getCustomEmojis(): Response<List<TootEmojiDTO>>

    @POST("api/v1/apps")
    suspend fun createApp(@Body body: CreateApp): Response<App>

    @POST("oauth/token")
    suspend fun obtainToken(@Body body: ObtainToken): Response<AccessToken>


    @GET("api/v1/accounts/verify_credentials")
    suspend fun verifyCredentials(): Response<MastodonAccountDTO>
}