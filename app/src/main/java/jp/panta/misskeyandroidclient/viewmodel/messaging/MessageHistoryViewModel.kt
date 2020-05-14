package jp.panta.misskeyandroidclient.viewmodel.messaging

import android.util.Log
import androidx.lifecycle.*
import io.reactivex.disposables.CompositeDisposable
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.RequestMessageHistory
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class MessageHistoryViewModel(
    private val accountRelation: AccountRelation,
    private val miCore: MiCore,
    private val encryption: Encryption = miCore.getEncryption()

    ) : ViewModel(){

    private val mDisposable = CompositeDisposable()

    val connectionInformation = accountRelation.getCurrentConnectionInformation()
    val historyUserLiveData = MutableLiveData<List<HistoryViewData>>()
    val historyGroupLiveData = MutableLiveData<List<HistoryViewData>>()

    val historyGroupAndUserLiveData = object : MediatorLiveData<List<HistoryViewData>>(){
        override fun onActive() {
            super.onActive()
            val disposable = miCore.messageSubscriber.getAccountMessageObservable(accountRelation)
                .subscribe { msg ->
                    val messagingId = msg.messagingId(accountRelation.account)
                    fun updateLiveData(liveData: MutableLiveData<List<HistoryViewData>>, message: Message){
                        val list = ArrayList<HistoryViewData>(liveData.value?: emptyList())
                        val anyMsg = list.firstOrNull { hvd ->
                            hvd.messagingId == messagingId
                        }
                        if( anyMsg == null ){
                            list.add(HistoryViewData(accountRelation.account, message))
                        }else{
                            anyMsg.message.postValue(message)
                        }
                        liveData.postValue(list)
                    }
                    if(messagingId.isGroup){
                        updateLiveData(historyGroupLiveData, msg)
                    }else{
                        updateLiveData(historyUserLiveData, msg)
                    }

                }
            mDisposable.add(disposable)
        }

        override fun onInactive() {
            super.onInactive()
            mDisposable.clear()
        }
    }.apply{
        addSource(historyUserLiveData){
            val groups = historyGroupLiveData.value?: emptyList()
            val list = ArrayList<HistoryViewData>()
            list.addAll(groups)
            list.addAll(it)
            this.postValue(list)
        }

        addSource(historyGroupLiveData){
            val users = historyUserLiveData.value?: emptyList()
            val list = ArrayList<HistoryViewData>().apply {
                addAll(it)
                addAll(users)
            }
            this.postValue(list)
        }
    }

    val isRefreshing = MutableLiveData<Boolean>(false)

    val messageHistorySelected = EventBus<HistoryViewData>()


    fun loadGroupAndUser(){
        isRefreshing.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            try{
                loadGroup()
                loadUser()

                isRefreshing.postValue(false)

            }catch(e: Exception){
                //セイバーかわいい
                Log.d("HistoryViewModel", "load error", e)
                isRefreshing.postValue(false)
            }
        }
    }

    fun loadGroup(){
        isRefreshing.postValue(true)
        val request = RequestMessageHistory(i = connectionInformation?.getI(encryption)!!, group = true, limit = 100)
        getMisskeyAPI()?.getMessageHistory(request)?.enqueue(object : Callback<List<Message>>{
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                val list = response.body()
                if(list != null){
                    historyGroupLiveData.postValue(list.map{
                        HistoryViewData(accountRelation.account, it)
                    })
                }
                isRefreshing.postValue(false)
            }

            override fun onFailure(call: Call<List<Message>>, t: Throwable) {
                Log.d("MessageHistory", "group historyの取得に失敗しました・・なんで！？:$call")
                isRefreshing.postValue(false)
            }
        })
    }

    fun loadUser(){
        isRefreshing.postValue(true)
        val request = RequestMessageHistory(i = connectionInformation?.getI(encryption)!!, group = false, limit = 100)
        getMisskeyAPI()?.getMessageHistory(request)?.enqueue(object : Callback<List<Message>>{
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                val list = response.body()
                if(list!= null){
                    historyUserLiveData.postValue(list.map{
                        HistoryViewData(accountRelation.account, it)
                    })
                }
                isRefreshing.postValue(false)

            }

            override fun onFailure(call: Call<List<Message>>, t: Throwable) {
                isRefreshing.postValue(false)
                Log.d("MessageHistory", "user historyの取得に失敗しました・・セイバーかわいい:$call")
            }
        })
    }

    fun openMessage(messageHistory: HistoryViewData){
        messageHistorySelected.event = messageHistory
    }

    private fun getMisskeyAPI(): MisskeyAPI?{
        return miCore.getMisskeyAPI(accountRelation)
    }
}