package jp.panta.misskeyandroidclient.viewmodel.messaging

import android.util.Log
import androidx.lifecycle.*
import io.reactivex.disposables.CompositeDisposable
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.MessageRelation
import jp.panta.misskeyandroidclient.model.messaging.RequestMessageHistory
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

@Suppress("BlockingMethodInNonBlockingContext")
@ExperimentalCoroutinesApi
@FlowPreview
class MessageHistoryViewModel(
    private val account: Account,
    private val miCore: MiCore,
    private val encryption: Encryption = miCore.getEncryption(),
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO

) : ViewModel(){


    private val historyUserLiveData = MutableLiveData<List<HistoryViewData>>()
    private val historyGroupLiveData = MutableLiveData<List<HistoryViewData>>()

    private val logger = miCore.loggerFactory.create("MessageHistoryViewModel")



    @FlowPreview
    val historyGroupAndUserLiveData = MediatorLiveData<List<HistoryViewData>>().apply{
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

    init {
        miCore.messageStreamFilter.getAccountMessageObservable(account).map {
            miCore.getGetters().messageRelationGetter.get(it)
        }.onEach {
            if(it is MessageRelation.Group) {
                updateLiveData(historyGroupLiveData, it)
            }else{
                updateLiveData(historyUserLiveData, it)
            }
        }.launchIn(viewModelScope + coroutineDispatcher)

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

    private fun loadGroup(){
        isRefreshing.postValue(true)
        viewModelScope.launch(coroutineDispatcher) {
            val messages = fetchHistory(true)
            if(messages.isNotEmpty()) {
                historyGroupLiveData.postValue(messages)
            }

            isRefreshing.postValue(false)

        }

    }

    private fun loadUser(){
        isRefreshing.postValue(true)
        viewModelScope.launch(coroutineDispatcher) {
            val messages = fetchHistory(false)
            if(messages.isNotEmpty()) {
                historyUserLiveData.postValue(messages)
            }
            isRefreshing.postValue(false)
        }
    }

    private suspend fun fetchHistory(isGroup: Boolean): List<HistoryViewData>{
        val request = RequestMessageHistory(i = account.getI(encryption), group = isGroup, limit = 100)

        return runCatching {
            val res = getMisskeyAPI().getMessageHistory(request).execute()
            res?.throwIfHasError()
                res?.body()?.map {
                miCore.getGetters().messageRelationGetter.get(account, it)
            }
        }.onFailure {
            logger.error("fetchMessagingHistory error", e = it)
        }.getOrNull()?.map {
            HistoryViewData(account, it, miCore.getUnreadMessages(), viewModelScope)
        }?: emptyList()
    }

    fun openMessage(messageHistory: HistoryViewData){
        messageHistorySelected.event = messageHistory
    }

    private fun getMisskeyAPI(): MisskeyAPI{
        return miCore.getMisskeyAPI(account)
    }

    private fun updateLiveData(liveData: MutableLiveData<List<HistoryViewData>>, message: MessageRelation){
        val list = ArrayList<HistoryViewData>(liveData.value?: emptyList())
        val anyMsg = list.firstOrNull { hvd ->
            hvd.messagingId == message.message.messagingId(account)
        }
        if( anyMsg == null ){

            list.add(HistoryViewData(account, message, miCore.getUnreadMessages(), viewModelScope, coroutineDispatcher))
        }else{
            anyMsg.message.postValue(message)
        }
        liveData.postValue(list)
    }
}