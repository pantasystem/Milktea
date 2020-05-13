package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.RequestMessage
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class MessageViewModel(
    private val accountRelation: AccountRelation,
    private val misskeyAPI: MisskeyAPI,
    messageHistory: Message,
    private val miCore: MiCore,
    private val encryption: Encryption = miCore.getEncryption()

) : ViewModel(){

    class State(
        val messages: List<MessageViewData>,
        val type: Type
    ){
        enum class Type{
            LOAD_INIT, LOAD_OLD, LOAD_NEW, RECEIVED
        }
    }

    private val mMessageId = messageHistory.messagingId(accountRelation.account)
    val connectionInformation = accountRelation.getCurrentConnectionInformation()

    val messagesLiveData = MutableLiveData<State>()
    private val builder = RequestMessage.Builder(accountRelation, messageHistory).apply{
        this.limit = 20
    }

    private var isLoading = false

    private val messageObserver = MessageObserver()

    //private val mainCapture = miCore.getMainCapture(accountRelation)
    private val mCompositeDisposable = CompositeDisposable()
    init{
        //mainCapture.putListener(messageObserver)
        val dis = miCore.messageSubscriber.getObservable(messageHistory.messagingId(accountRelation.account), accountRelation).subscribe { message ->
            val messages = messagesLiveData.value?.messages.toArrayList()


            if(message.messagingId(accountRelation.account) == mMessageId){
                val msg = if(message.userId == accountRelation.account.id){
                    //me
                    SelfMessageViewData(message, accountRelation.account)
                }else{
                    RecipientMessageViewData(message, accountRelation.account)
                }
                messages.add(msg)

                messagesLiveData.postValue(State(messages, State.Type.RECEIVED))

            }
        }
        mCompositeDisposable.add(dis)
    }

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
                        SelfMessageViewData(it, accountRelation.account)
                    }else{
                        RecipientMessageViewData(it, accountRelation.account)
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
                        SelfMessageViewData(it, accountRelation.account)
                    }else{
                        RecipientMessageViewData(it, accountRelation.account)
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
                        SelfMessageViewData(it, accountRelation.account)
                    }else{
                        RecipientMessageViewData(it, accountRelation.account)
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


            if(message.messagingId(accountRelation.account) == mMessageId){
                val msg = if(message.userId == accountRelation.account.id){
                    //me
                    SelfMessageViewData(message, accountRelation.account)
                }else{
                    RecipientMessageViewData(message, accountRelation.account)
                }
                messages.add(msg)

                messagesLiveData.postValue(State(messages, State.Type.RECEIVED))

            }


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
