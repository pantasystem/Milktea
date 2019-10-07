package jp.panta.misskeyandroidclient.viewmodel.notes

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.notes.LiveNotePagingStore
import jp.panta.misskeyandroidclient.model.notes.Note
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

class TimelineViewModel(type: Type) : ViewModel(){
    enum class Type{
        HOME,
        LOCAL,
        SOCIAL,
        GLOBAL
    }

    val timeline: LiveData<List<PlaneNoteViewData>>

    val isLoading = MediatorLiveData<Boolean>()

    val errorState = MediatorLiveData<String>()

    private val baseUrl = "https://misskey.io/"

    private val misskeyAPI = MisskeyAPIServiceBuilder.build(baseUrl)
    val i = ""

    private val pagingCallBack = object : LiveNotePagingStore.CallBack{
        override fun onLoad() {
            isLoading.postValue(false)
        }
        override fun onError(t: Throwable) {
            isLoading.postValue(false)
            errorState.postValue("通信エラー")
        }
    }

    private val mLivePagingStore = when(type){
        Type.HOME -> LiveNotePagingStore(i, misskeyAPI::homeTimeline, pagingCallBack)
        Type.LOCAL -> LiveNotePagingStore(i, misskeyAPI::localTimeline, pagingCallBack)
        Type.SOCIAL -> LiveNotePagingStore(i, misskeyAPI::hybridTimeline, pagingCallBack)
        Type.GLOBAL -> LiveNotePagingStore(i, misskeyAPI::globalTimeline, pagingCallBack)

    }

    init{
        val liveData = MediatorLiveData<List<PlaneNoteViewData>>()
        mLivePagingStore.setLiveData(liveData)
        timeline = liveData

    }


    fun loadNew(){
        this.isLoading.postValue(true)
        this.mLivePagingStore.loadNew()
    }

    fun loadOld(){
        this.mLivePagingStore.loadOld()
    }

    fun loadInit(){
        this.isLoading.postValue(true)
        this.mLivePagingStore.loadInit()
    }


}