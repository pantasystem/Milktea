package jp.panta.misskeyandroidclient.api.mastodon

import jp.panta.misskeyandroidclient.api.mastodon.apps.AccessToken
import jp.panta.misskeyandroidclient.api.mastodon.apps.App
import jp.panta.misskeyandroidclient.api.mastodon.apps.CreateApp
import jp.panta.misskeyandroidclient.api.mastodon.apps.ObtainToken
import jp.panta.misskeyandroidclient.api.mastodon.emojis.TootEmojiDTO
import jp.panta.misskeyandroidclient.api.mastodon.instance.Instance
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

}