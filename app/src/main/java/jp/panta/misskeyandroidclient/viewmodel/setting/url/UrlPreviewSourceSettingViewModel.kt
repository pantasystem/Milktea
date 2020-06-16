package jp.panta.misskeyandroidclient.viewmodel.setting.url

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

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return UrlPreviewSourceSettingViewModel(miApplication, miApplication.settingStore) as T
        }
    }

    private var mUrlPreviewStore = miCore.urlPreviewStore

    val urlPreviewSourceType = MutableLiveData<Int>(settingStore.urlPreviewSetting.getSourceType())

    private var mSummalyServerUrl: String? = settingStore.urlPreviewSetting.getSummalyUrl()
    val summalyServerUrl = MediatorLiveData<String?>().apply{
        addSource(urlPreviewSourceType){
            value = if(it == UrlPreviewSourceSetting.SUMMALY){
                mSummalyServerUrl?: settingStore.urlPreviewSetting.getSummalyUrl()?: ""
            }else{
                mSummalyServerUrl = value
                null
            }
            mUrlPreviewStore = UrlPreviewStoreFactory(
                it,
                this.value,
                miCore.currentAccount.value
            ).create()
        }

    }

    val previewTestUrl = MutableLiveData<String?>(
        miCore.currentAccount.value?.getCurrentConnectionInformation()?.instanceBaseUrl
    )

    val urlPreviewData = MediatorLiveData<UrlPreview>().apply{
        addSource(previewTestUrl){
            loadPreview()
        }
        addSource(urlPreviewSourceType){
            loadPreview()
        }
    }

    fun loadPreview(){
        viewModelScope.launch(Dispatchers.IO) {
            try{
                previewTestUrl.value?.let{
                    urlPreviewData.postValue(
                        mUrlPreviewStore?.get(it)
                    )
                }

            }catch (e: Exception){

            }
        }
    }



}