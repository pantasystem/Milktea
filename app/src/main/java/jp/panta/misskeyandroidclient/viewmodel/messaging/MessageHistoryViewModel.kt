package jp.panta.misskeyandroidclient.viewmodel.messaging

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.RequestMessage
import jp.panta.misskeyandroidclient.model.messaging.RequestMessageHistory
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class MessageHistoryViewModel(
    private val accountRelation: AccountRelation,
    private val misskeyAPI: MisskeyAPI,
    private val encryption: Encryption
) : ViewModel(){
    val connectionInformation = accountRelation.getCurrentConnectionInformation()
    val historyUserLiveData = MutableLiveData<List<HistoryViewData>>()
    val historyGroupLiveData = MutableLiveData<List<HistoryViewData>>()
    val historyGroupAndUserLiveData = MutableLiveData<List<HistoryViewData>>()

    val isRefreshing = MutableLiveData<Boolean>(false)

    val messageHistorySelected = EventBus<HistoryViewData>()

    fun loadGroupAndUser(){
        isRefreshing.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            try{
                val groupRequest = RequestMessageHistory(i = connectionInformation?.getI(encryption)!!, group = true, limit = 100)
                val userRequest = RequestMessageHistory(i = connectionInformation.getI(encryption)!!, group = false, limit = 100)

                val groupHistory = misskeyAPI.getMessageHistory(groupRequest).execute().body()
                val userHistory = misskeyAPI.getMessageHistory(userRequest).execute().body()

                val history = if(groupHistory == null){
                    ArrayList()
                }
                else{
                    ArrayList(groupHistory.map{
                        HistoryViewData(accountRelation.account, it)
                    })
                }

                if(userHistory != null){
                    history.addAll(userHistory.map{
                        HistoryViewData(accountRelation.account, it)
                    })
                }
                historyGroupAndUserLiveData.postValue(history)

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
        misskeyAPI.getMessageHistory(request).enqueue(object : Callback<List<Message>>{
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
        misskeyAPI.getMessageHistory(request).enqueue(object : Callback<List<Message>>{
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

}