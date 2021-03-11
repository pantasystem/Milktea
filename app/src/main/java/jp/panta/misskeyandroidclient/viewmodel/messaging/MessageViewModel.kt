package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.api.messaging.RequestMessage
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class MessageViewModel(
    private val account: Account,
    private val misskeyAPI: MisskeyAPI,
    messageHistory: MessageDTO,
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

    private val mMessageId = messageHistory.messagingId(account)

    val messagesLiveData = MutableLiveData<State>()
    private val builder = RequestMessage.Builder(account, messageHistory).apply{
        this.limit = 20
    }

    private var isLoading = false


    private val unreadMessageStore = miCore.messageStreamFilter.getUnreadMessageStore(account)
    //private val mainCapture = miCore.getMainCapture(accountRelation)
    private val mCompositeDisposable = CompositeDisposable()
    init{
        //mainCapture.putListener(messageObserver)
        val dis = miCore.messageStreamFilter.getObservable(messageHistory.messagingId(account), account).subscribe { message ->
            val messages = messagesLiveData.value?.messages.toArrayList()


            if(message.messagingId(account) == mMessageId){
                val msg = if(message.userId == account.remoteId){
                    //me
                    SelfMessageViewData(message, account)
                }else{
                    RecipientMessageViewData(message, account)
                }
                messages.add(msg)

                messagesLiveData.postValue(State(messages, State.Type.RECEIVED))
                unreadMessageStore.readMessage(message)

            }
        }
        mCompositeDisposable.add(dis)
    }

    fun loadInit(){
        if(isLoading){
            return
        }
        isLoading = true
        misskeyAPI.getMessages(builder.build(null, null, encryption)).enqueue(object : Callback<List<MessageDTO>>{
            override fun onResponse(call: Call<List<MessageDTO>>, response: Response<List<MessageDTO>>) {
                val rawMessages = response.body()?.asReversed()
                if(rawMessages == null){
                    isLoading = false
                    return
                }
                unreadMessageStore.readAll(mMessageId)
                val viewDataList = rawMessages.map{
                    if(it.user?.id == account.remoteId){
                        //me
                        SelfMessageViewData(it, account)
                    }else{
                        RecipientMessageViewData(it, account)
                    }
                }
                messagesLiveData.postValue(State(viewDataList, State.Type.LOAD_INIT))
                isLoading = false
            }

            override fun onFailure(call: Call<List<MessageDTO>>, t: Throwable) {
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

        misskeyAPI.getMessages(builder.build(untilId = untilId, sinceId = null, encryption = encryption)).enqueue(object : Callback<List<MessageDTO>>{
            override fun onResponse(call: Call<List<MessageDTO>>, response: Response<List<MessageDTO>>) {
                val reversedMessages = response.body()?.asReversed()
                if(reversedMessages == null){
                    isLoading = false
                    return
                }
                val viewData = reversedMessages.map{
                    if(it.userId == account.remoteId){
                        SelfMessageViewData(it, account)
                    }else{
                        RecipientMessageViewData(it, account)
                    }
                }

                val messages = ArrayList<MessageViewData>(exMessages).apply{
                    addAll(0, viewData)
                }
                messagesLiveData.postValue(State(messages, State.Type.LOAD_OLD))
                isLoading = false
            }

            override fun onFailure(call: Call<List<MessageDTO>>, t: Throwable) {
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
        misskeyAPI.getMessages(builder.build(sinceId = sinceId, untilId = null, encryption = encryption)).enqueue(object : Callback<List<MessageDTO>>{
            override fun onResponse(call: Call<List<MessageDTO>>, response: Response<List<MessageDTO>>) {
                val rawList = response.body()
                if(rawList == null){
                    isLoading = false
                    return
                }

                val viewData = rawList.map{
                    if(it.userId == account.remoteId){
                        //me
                        SelfMessageViewData(it, account)
                    }else{
                        RecipientMessageViewData(it, account)
                    }
                }

                val messages = ArrayList<MessageViewData>(exMessages).apply{
                    addAll(viewData)
                }
                messagesLiveData.postValue(State(messages, State.Type.LOAD_NEW))
            }

            override fun onFailure(call: Call<List<MessageDTO>>, t: Throwable) {

            }
        })
    }



    private fun List<MessageViewData>?.toArrayList(): ArrayList<MessageViewData>{
        return if(this == null){
            ArrayList()
        }else{
            ArrayList(this)
        }
    }
}
