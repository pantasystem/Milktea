package jp.panta.misskeyandroidclient.viewmodel.messaging

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.RequestMessage
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class MessageViewModel(
    private val accountRelation: AccountRelation,
    private val misskeyAPI: MisskeyAPI,
    messageHistory: Message,
    private val encryption: Encryption

) : ViewModel(){

    class State(
        val messages: List<MessageViewData>,
        val type: Type
    ){
        enum class Type{
            LOAD_INIT, LOAD_OLD, LOAD_NEW, RECEIVED
        }
    }
    val connectionInformation = accountRelation.getCurrentConnectionInformation()

    val messagesLiveData = object :MutableLiveData<State>(){
        override fun onActive() {
            super.onActive()

        }
        override fun onInactive() {
            super.onInactive()
        }
    }
    private val builder = RequestMessage.Builder(accountRelation, messageHistory).apply{
        this.limit = 20
    }

    private val observerId = UUID.randomUUID().toString()
    val streamingAdapter =  StreamingAdapter(connectionInformation, encryption).apply{
        val main = MainCapture(GsonFactory.create())
        addObserver(observerId, main)
        main.addListener(MessageObserver())
        connect()
    }


    private var isLoading = false


    fun loadInit(){
        if(isLoading){
            return
        }
        isLoading = true
        misskeyAPI.getMessages(builder.build(null, null, encryption)).enqueue(object : Callback<List<Message>>{
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                val rawMessages = response.body()?.asReversed()
                if(rawMessages == null){
                    isLoading = false
                    return
                }
                val viewDataList = rawMessages.map{
                    if(it.user?.id == accountRelation.account.id){
                        //me
                        SelfMessageViewData(it)
                    }else{
                        RecipientMessageViewData(it)
                    }
                }
                messagesLiveData.postValue(State(viewDataList, State.Type.LOAD_INIT))
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
        val exMessages = messagesLiveData.value?.messages
        val untilId = exMessages?.firstOrNull()?.id
        if(exMessages.isNullOrEmpty() || untilId == null){
            isLoading = false
            return
        }

        misskeyAPI.getMessages(builder.build(untilId = untilId, sinceId = null, encryption = encryption)).enqueue(object : Callback<List<Message>>{
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                val reversedMessages = response.body()?.asReversed()
                if(reversedMessages == null){
                    isLoading = false
                    return
                }
                val viewData = reversedMessages.map{
                    if(it.userId == accountRelation.account.id){
                        SelfMessageViewData(it)
                    }else{
                        RecipientMessageViewData(it)
                    }
                }

                val messages = ArrayList<MessageViewData>(exMessages).apply{
                    addAll(0, viewData)
                }
                messagesLiveData.postValue(State(messages, State.Type.LOAD_OLD))
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
        val exMessages = messagesLiveData.value?.messages
        val sinceId = exMessages?.lastOrNull()?.id
        if(exMessages.isNullOrEmpty() || sinceId == null){
            isLoading = false
            return
        }
        misskeyAPI.getMessages(builder.build(sinceId = sinceId, untilId = null, encryption = encryption)).enqueue(object : Callback<List<Message>>{
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                val rawList = response.body()
                if(rawList == null){
                    isLoading = false
                    return
                }

                val viewData = rawList.map{
                    if(it.userId == accountRelation.account.id){
                        //me
                        SelfMessageViewData(it)
                    }else{
                        RecipientMessageViewData(it)
                    }
                }

                val messages = ArrayList<MessageViewData>(exMessages).apply{
                    addAll(viewData)
                }
                messagesLiveData.postValue(State(messages, State.Type.LOAD_NEW))
            }

            override fun onFailure(call: Call<List<Message>>, t: Throwable) {

            }
        })
    }

    inner class MessageObserver : MainCapture.AbsListener(){
        override fun messagingMessage(message: Message) {
            val messages = messagesLiveData.value?.messages.toArrayList()

            val msg = if(message.userId == accountRelation.account.id){
                //me
                SelfMessageViewData(message)
            }else{
                RecipientMessageViewData(message)
            }
            messages.add(msg)

            messagesLiveData.postValue(State(messages, State.Type.RECEIVED))
        }
    }

    private fun List<MessageViewData>?.toArrayList(): ArrayList<MessageViewData>{
        return if(this == null){
            ArrayList()
        }else{
            ArrayList(this)
        }
    }
}
