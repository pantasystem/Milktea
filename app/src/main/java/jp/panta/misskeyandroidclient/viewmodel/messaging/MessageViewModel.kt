package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.RequestMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageViewModel(
    private val connectionInstance: ConnectionInstance,
    private val misskeyAPI: MisskeyAPI,
    messageHistory: Message

) : ViewModel(){

    //reversed
    val messagesLiveData = MutableLiveData<List<MessageViewData>>()
    private val builder = RequestMessage.Builder(connectionInstance, messageHistory).apply{
        this.limit = 20
    }


    private var isLoading = false


    fun loadInit(){
        if(isLoading){
            return
        }
        isLoading = true
        misskeyAPI.getMessages(builder.build(null, null)).enqueue(object : Callback<List<Message>>{
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                val rawMessages = response.body()?.asReversed()
                if(rawMessages == null){
                    isLoading = false
                    return
                }
                val viewDataList = rawMessages.map{
                    if(it.user?.id == connectionInstance.userId){
                        //me
                        SelfMessageViewData(it)
                    }else{
                        RecipientMessageViewData(it)
                    }
                }
                messagesLiveData.postValue(viewDataList)
                isLoading = false
            }

            override fun onFailure(call: Call<List<Message>>, t: Throwable) {
                isLoading = false
            }
        })

    }

    fun loadOld(){
        if(isLoading){
            return
        }
        isLoading = true
        val exMessages = messagesLiveData.value
        val untilId = exMessages?.firstOrNull()?.id
        if(exMessages.isNullOrEmpty() || untilId == null){
            isLoading = false
            return
        }

        misskeyAPI.getMessages(builder.build(untilId = untilId, sinceId = null)).enqueue(object : Callback<List<Message>>{
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                val reversedMessages = response.body()?.asReversed()
                if(reversedMessages == null){
                    isLoading = false
                    return
                }
                val viewData = reversedMessages.map{
                    if(it.userId == connectionInstance.userId){
                        SelfMessageViewData(it)
                    }else{
                        RecipientMessageViewData(it)
                    }
                }

                val messages = ArrayList<MessageViewData>(exMessages).apply{
                    addAll(0, viewData)
                }
                messagesLiveData.postValue(messages)
                isLoading = false
            }

            override fun onFailure(call: Call<List<Message>>, t: Throwable) {
                isLoading = false
            }
        })

    }

    fun loadNew(){
        if(isLoading){
            return
        }
        isLoading = true
        val exMessages = messagesLiveData.value
        val sinceId = exMessages?.lastOrNull()?.id
        if(exMessages.isNullOrEmpty() || sinceId == null){
            isLoading = false
            return
        }
        misskeyAPI.getMessages(builder.build(sinceId = sinceId, untilId = null)).enqueue(object : Callback<List<Message>>{
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                val rawList = response.body()
                if(rawList == null){
                    isLoading = false
                    return
                }

                val viewData = rawList.map{
                    if(it.userId == connectionInstance.userId){
                        //me
                        SelfMessageViewData(it)
                    }else{
                        RecipientMessageViewData(it)
                    }
                }

                val messages = ArrayList<MessageViewData>(exMessages).apply{
                    addAll(viewData)
                }
                messagesLiveData.postValue(messages)
            }

            override fun onFailure(call: Call<List<Message>>, t: Throwable) {

            }
        })
    }
}
