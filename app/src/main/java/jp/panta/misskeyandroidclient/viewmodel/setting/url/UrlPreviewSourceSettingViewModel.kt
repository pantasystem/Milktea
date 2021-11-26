package jp.panta.misskeyandroidclient.viewmodel.setting.url

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.settings.UrlPreviewSourceSetting
import jp.panta.misskeyandroidclient.model.url.UrlPreview
import jp.panta.misskeyandroidclient.model.url.UrlPreviewStoreFactory
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UrlPreviewSourceSettingViewModel(val miCore: MiCore, val settingStore: SettingStore) : ViewModel(){


    @Suppress("UNCHECKED_CAST")
    class Factory(val miApplication: MiApplication) : ViewModelProvider.Factory{

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UrlPreviewSourceSettingViewModel(miApplication, miApplication.getSettingStore()) as T
        }
    }

    private var mUrlPreviewStore = miCore.getCurrentAccount().value?.let{ it ->
        miCore.getUrlPreviewStore(it)
    }
    val urlPreviewSourceType = object : MutableLiveData<Int>(settingStore.urlPreviewSetting.getSourceType()){
        override fun onInactive() {
            super.onInactive()
            save()
        }
    }

    private var mSummalyServerUrl: String? = settingStore.urlPreviewSetting.getSummalyUrl()
    val summalyServerUrl = MediatorLiveData<String?>().apply{
        addSource(urlPreviewSourceType){
            value = if(it == UrlPreviewSourceSetting.SUMMALY){
                mSummalyServerUrl?: settingStore.urlPreviewSetting.getSummalyUrl()?: ""
            }else{
                mSummalyServerUrl = value
                null
            }
            var url = this.value
            if(url != null){
                url = "https://$url"
            }
            mUrlPreviewStore = UrlPreviewStoreFactory(
                (miCore as MiApplication).urlPreviewDAO,
                it,
                url,
                miCore.getCurrentAccount().value
            ).create()
        }

    }

    val previewTestUrl = MutableLiveData<String?>(
        miCore.getCurrentAccount().value?.instanceDomain?.replace("https://", "")
    )

    val urlPreviewData = MediatorLiveData<UrlPreview>().apply{
        addSource(previewTestUrl){
            loadPreview()
        }
        addSource(urlPreviewSourceType){
            loadPreview()
        }
    }


    private var urlPreviewLoadCounter = 0

    private val releaseUrlPreviewLoadQueue = Handler(Looper.myLooper()!!)

    private val releaseUrlPreviewLoadCounter = Runnable {
        urlPreviewLoadCounter = 0

        // リミッターが解除されたタイミングでリクエストを送信する
        loadPreview()
    }

    fun loadPreview(){
        if(urlPreviewLoadCounter ++ > 0){
            releaseUrlPreviewLoadQueue.removeCallbacks(releaseUrlPreviewLoadCounter)
            releaseUrlPreviewLoadQueue.postDelayed(releaseUrlPreviewLoadCounter, 1000)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try{
                previewTestUrl.value?.let{
                    val preview = mUrlPreviewStore?.get("https://$it")
                    urlPreviewData.postValue(preview)

                }
            }catch (e: Exception){

            }
        }
    }

    fun setSourceType(type: Int){
        if(type in 0 until 3){
            urlPreviewSourceType.value = type
        }
    }

    private fun save(){
        val sourceType = urlPreviewSourceType.value?: UrlPreviewSourceSetting.APP

        if( sourceType == UrlPreviewSourceSetting.SUMMALY ){
            summalyServerUrl.value?.let{
                settingStore.urlPreviewSetting.setSummalyUrl(it)
                return
            }
        }

        settingStore.urlPreviewSetting.setSourceType(sourceType)
    }


}