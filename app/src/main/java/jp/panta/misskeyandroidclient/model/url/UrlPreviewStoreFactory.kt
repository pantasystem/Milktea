package jp.panta.misskeyandroidclient.model.url

import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.settings.UrlPreviewSourceSetting
import jp.panta.misskeyandroidclient.model.url.db.UrlPreviewDAO
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

class UrlPreviewStoreFactory (
    private val urlPreviewDAO: UrlPreviewDAO,
    sourceType: Int? = null,
    private var summalyUrl: String? = null,
    var account: Account? = null
){

    private var sourceType = sourceType?: UrlPreviewSourceSetting.MISSKEY

    fun create(): UrlPreviewStore{
        val url = when(sourceType){
            UrlPreviewSourceSetting.MISSKEY ->{
                account?.instanceDomain
                    ?: summalyUrl
            }
            UrlPreviewSourceSetting.SUMMALY ->{
                summalyUrl
                    ?: account?.instanceDomain
            }
            else -> null
        }
        return UrlPreviewMediatorStore(urlPreviewDAO, createUrlPreviewStore(url))
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