package jp.panta.misskeyandroidclient.viewmodel.messaging

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.groups.toGroup
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.messaging.MessageHistoryRelation
import jp.panta.misskeyandroidclient.model.messaging.RequestMessageHistory
import jp.panta.misskeyandroidclient.model.messaging.toHistory
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

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
        }.map {
            it.toHistory(miCore.getGroupRepository(), miCore.getUserRepository())
        }.onEach {
            if(it is MessageHistoryRelation.Group) {
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
            val res = getMisskeyAPI().getMessageHistory(request)
            res.throwIfHasError()
            res.body()?.map {
                it.group?.let {  groupDTO ->
                    miCore.getGroupDataSource().add(groupDTO.toGroup(account.accountId))
                }
                it.recipient?.let { userDTO ->
                    miCore.getUserDataSource().add(userDTO.toUser(account))
                }
                miCore.getGetters().messageRelationGetter.get(account, it)
            }
        }.onFailure {
            logger.error("fetchMessagingHistory error", e = it)
        }.getOrNull()?.map {
            it.toHistory(miCore.getGroupRepository(), miCore.getUserRepository())
        }?.map {
            HistoryViewData(account, it, miCore.getUnreadMessages(), viewModelScope)
        }?: emptyList()
    }

    fun openMessage(messageHistory: HistoryViewData){
        messageHistorySelected.event = messageHistory
    }

    private fun getMisskeyAPI(): MisskeyAPI {
        return miCore.getMisskeyAPI(account)
    }

    private fun updateLiveData(liveData: MutableLiveData<List<HistoryViewData>>, message: MessageHistoryRelation){
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