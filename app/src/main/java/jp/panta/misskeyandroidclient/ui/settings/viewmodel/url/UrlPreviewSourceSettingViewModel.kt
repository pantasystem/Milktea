package jp.panta.misskeyandroidclient.ui.settings.viewmodel.url

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.data.model.settings.SettingStore
import net.pantasystem.milktea.data.model.settings.UrlPreviewSourceSetting
import net.pantasystem.milktea.data.model.url.UrlPreview
import net.pantasystem.milktea.data.model.url.UrlPreviewStoreFactory
import net.pantasystem.milktea.data.model.url.db.UrlPreviewDAO
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UrlPreviewSourceSettingViewModel @Inject constructor(
    val accountStore: net.pantasystem.milktea.model.account.AccountStore,
    val settingStore: SettingStore,
    val urlPreviewDAO: UrlPreviewDAO,
    val miCore: MiCore

) :
    ViewModel() {


    private var mUrlPreviewStore = accountStore.currentAccount?.let { it ->
        miCore.getUrlPreviewStore(it)
    }
    val urlPreviewSourceType =
        object : MutableLiveData<Int>(settingStore.urlPreviewSetting.getSourceType()) {
            override fun onInactive() {
                super.onInactive()
                save()
            }
        }

    private var mSummalyServerUrl: String? = settingStore.urlPreviewSetting.getSummalyUrl()
    val summalyServerUrl = MediatorLiveData<String?>().apply {
        addSource(urlPreviewSourceType) {
            value = if (it == UrlPreviewSourceSetting.SUMMALY) {
                mSummalyServerUrl ?: settingStore.urlPreviewSetting.getSummalyUrl() ?: ""
            } else {
                mSummalyServerUrl = value
                null
            }
            var url = this.value
            if (url != null) {
                url = "https://$url"
            }
            mUrlPreviewStore = UrlPreviewStoreFactory(
                urlPreviewDAO,
                it,
                url,
                accountStore.currentAccount
            ).create()
        }

    }

    val previewTestUrl = MutableLiveData<String?>(
        accountStore.currentAccount?.instanceDomain?.replace("https://", "")
    )

    val urlPreviewData = MediatorLiveData<UrlPreview>().apply {
        addSource(previewTestUrl) {
            loadPreview()
        }
        addSource(urlPreviewSourceType) {
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

    private fun loadPreview() {
        if (urlPreviewLoadCounter++ > 0) {
            releaseUrlPreviewLoadQueue.removeCallbacks(releaseUrlPreviewLoadCounter)
            releaseUrlPreviewLoadQueue.postDelayed(releaseUrlPreviewLoadCounter, 1000)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                previewTestUrl.value?.let {
                    val preview = mUrlPreviewStore?.get("https://$it")
                    urlPreviewData.postValue(preview)

                }
            } catch (e: Exception) {

            }
        }
    }

    fun setSourceType(type: Int) {
        if (type in 0 until 3) {
            urlPreviewSourceType.value = type
        }
    }

    private fun save() {
        val sourceType = urlPreviewSourceType.value ?: UrlPreviewSourceSetting.APP

        if (sourceType == UrlPreviewSourceSetting.SUMMALY) {
            summalyServerUrl.value?.let {
                settingStore.urlPreviewSetting.setSummalyUrl(it)
                return
            }
        }

        settingStore.urlPreviewSetting.setSourceType(sourceType)
    }


}