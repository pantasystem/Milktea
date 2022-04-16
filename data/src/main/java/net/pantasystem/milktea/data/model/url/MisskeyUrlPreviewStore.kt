package net.pantasystem.milktea.data.model.url

import android.util.Log
import java.io.IOException
import java.lang.Exception

class MisskeyUrlPreviewStore(
    private val retrofitMisskeyUrlPreview: RetrofitMisskeyUrlPreview
) : UrlPreviewStore{

    override fun get(url: String): UrlPreview? {
        return try {
            return retrofitMisskeyUrlPreview.getUrl(url).execute().body()
        } catch (e: Exception){
            Log.d("MisskeyUrlPreviewStore", "get url preview error", e)
            null
        }
    }
}