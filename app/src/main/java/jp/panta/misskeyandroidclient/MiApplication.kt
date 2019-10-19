package jp.panta.misskeyandroidclient

import android.app.Application
import android.util.Log
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.meta.RequestMeta
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//基本的な情報はここを返して扱われる
class MiApplication : Application(){

    private var nowInstance = "https://misskey.io"
    private val misskeyAPIService = MisskeyAPIServiceBuilder.build(nowInstance)

    var nowInstanceMeta: Meta? = null

    override fun onCreate() {
        super.onCreate()

        setMeta()
    }

    private fun setMeta(){
        misskeyAPIService.getMeta(RequestMeta())
            .enqueue(object : Callback<Meta>{
                override fun onResponse(call: Call<Meta>, response: Response<Meta>) {
                    nowInstanceMeta = response.body()
                    Log.d("MiApplication", "$nowInstanceMeta")
                }
                override fun onFailure(call: Call<Meta>, t: Throwable) {
                    Log.w("MiApplication", "metaの取得に失敗した", t)
                }
            })
    }
}