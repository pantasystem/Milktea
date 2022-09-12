package jp.panta.misskeyandroidclient.ui.settings.viewmodel.url

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.app_store.setting.UrlPreviewSourceSetting
import net.pantasystem.milktea.data.infrastructure.url.UrlPreviewStoreFactory
import net.pantasystem.milktea.data.infrastructure.url.UrlPreviewStoreProvider
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewDAO
import net.pantasystem.milktea.model.url.UrlPreview
import javax.inject.Inject

@HiltViewModel
class UrlPreviewSourceSettingViewModel @Inject constructor(
    val accountStore: AccountStore,
    val settingStore: SettingStore,
    private val urlPreviewDAO: UrlPreviewDAO,
    private val urlPreviewStoreProvider: UrlPreviewStoreProvider,
    private val misskeyAPIServiceBuilder: MisskeyAPIServiceBuilder,

    ) :
    ViewModel() {


    private var mUrlPreviewStore = accountStore.currentAccount?.let { it ->
        urlPreviewStoreProvider.getUrlPreviewStore(it)
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
                accountStore.currentAccount,
                misskeyAPIServiceBuilder
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