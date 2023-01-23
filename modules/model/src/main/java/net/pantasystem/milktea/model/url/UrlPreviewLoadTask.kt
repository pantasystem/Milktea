package net.pantasystem.milktea.model.url

import android.util.Log
import kotlinx.coroutines.*

class UrlPreviewLoadTask(
    private val urlPreviewStore: UrlPreviewStore?,
    private val urlList: List<String>?,
    private val coroutineScope: CoroutineScope
) {

    private var job: Job? = null

    @FunctionalInterface
    interface Callback{
        fun accept(list: List<UrlPreview>)
    }

    fun load(callback: Callback){
        job = loadUrlPreviews(callback)
    }

    fun cancel(){
        job?.cancel()
    }

    private fun loadUrlPreviews(callback: Callback)= coroutineScope.launch(Dispatchers.IO){
        try{
            urlPreviewStore?: return@launch
            urlList?: return@launch
            val list = urlList.mapIndexed { i, url ->
                coroutineScope.async(Dispatchers.IO) {
                    try{
                        i to urlPreviewStore.get(url)
                    }catch(e: Exception){
                        Log.e("UrlPreviewLoadTask", "error", e)
                        null
                    }
                }
            }.awaitAll().filterNotNull().sortedBy {
                it.first
            }.mapNotNull {
                it.second
            }.filterNot {
                it.url.isBlank()
            }
            //Log.d("UrlPreviewLoadTask", list.toString())
            callback.accept(list)

        }catch(e: Throwable){
            Log.d("UrlPreviewLoadTask", "url load error", e)
        }

    }
}