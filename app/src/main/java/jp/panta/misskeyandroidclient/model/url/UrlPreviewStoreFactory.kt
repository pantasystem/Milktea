package jp.panta.misskeyandroidclient.model.url

import android.util.Log
import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.settings.UrlPreviewSourceSetting
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

class UrlPreviewStoreFactory (
    sourceType: Int? = null,
    var summalyUrl: String? = null,
    var accountRelation: AccountRelation? = null
){

    private var sourceType = sourceType?: UrlPreviewSourceSetting.MISSKEY

    fun create(): UrlPreviewStore{
        val url = when(sourceType){
            UrlPreviewSourceSetting.MISSKEY ->{
                accountRelation?.getCurrentConnectionInformation()?.instanceBaseUrl
                    ?: summalyUrl
            }
            UrlPreviewSourceSetting.SUMMALY ->{
                summalyUrl
                    ?: accountRelation?.getCurrentConnectionInformation()?.instanceBaseUrl
            }
            else -> null
        }
        return createUrlPreviewStore(url)
    }

    private fun createUrlPreviewStore(url: String?): UrlPreviewStore{
        return when(sourceType){
            UrlPreviewSourceSetting.MISSKEY, UrlPreviewSourceSetting.SUMMALY ->{
                try{
                    MisskeyUrlPreviewStore(
                        Retrofit.Builder()
                            .baseUrl(url!!)
                            .addConverterFactory(GsonConverterFactory.create(GsonFactory.create()))
                            .client(OkHttpClient.Builder().build())
                            .build()
                            .create(RetrofitMisskeyUrlPreview::class.java)
                    )
                }catch (e: Exception){
                    JSoupUrlPreviewStore()
                }

            }
            else ->{
                JSoupUrlPreviewStore()
            }
        }
    }
}