package net.pantasystem.milktea.data.infrastructure.url

import android.util.Log
import net.pantasystem.milktea.model.url.UrlPreview
import net.pantasystem.milktea.model.url.UrlPreviewStore
import java.lang.Exception

class MisskeyUrlPreviewStore(
    private val retrofitMisskeyUrlPreview: RetrofitMisskeyUrlPreview
) : UrlPreviewStore {

    override fun get(url: String): UrlPreview? {
        return try {
            return retrofitMisskeyUrlPreview.getUrl(url).execute().body()
        } catch (e: Exception){
            Log.d("MisskeyUrlPreviewStore", "get url preview error", e)
            null
        }
    }
}