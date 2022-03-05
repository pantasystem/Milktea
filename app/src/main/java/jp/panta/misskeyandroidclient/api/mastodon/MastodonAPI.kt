package jp.panta.misskeyandroidclient.api.mastodon

import jp.panta.misskeyandroidclient.api.mastodon.emojis.TootEmojiDTO
import jp.panta.misskeyandroidclient.api.mastodon.instance.Instance

import retrofit2.http.GET

interface MastodonAPI {

    @GET("api/v1/instance")
    suspend fun getInstance(): Instance

    @GET("api/v1/custom_emojis")
    suspend fun getCustomEmojis(): List<TootEmojiDTO>
}