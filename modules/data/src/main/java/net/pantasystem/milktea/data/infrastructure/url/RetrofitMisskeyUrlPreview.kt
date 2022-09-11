package net.pantasystem.milktea.data.infrastructure.url

import net.pantasystem.milktea.model.url.UrlPreview
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitMisskeyUrlPreview {

    //https://misskey.io/url?url=https%3A%2F%2Ftwitter.com%2F_namori_%2Fstatus%2F1270603288040230913%2Fphoto%2F1&lang=ja-JP
    @GET("/url")
    fun getUrl(@Query("url") url: String): Call<UrlPreview>
}