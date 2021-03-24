package jp.panta.misskeyandroidclient.viewmodel.messaging

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.api.messaging.MessageAction
import retrofit2.Call
import java.lang.IllegalArgumentException
import retrofit2.Callback
import retrofit2.Response
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.messaging.CreateMessage
import jp.panta.misskeyandroidclient.model.messaging.MessagingId
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessageActionViewModel(

    private val messagingId: MessagingId,
    private val miCore: MiCore
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val messagingId: MessagingId,
        val miApplication: MiApplication,
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass == MessageActionViewModel::class.java){
                return MessageActionViewModel(messagingId, miApplication) as T
            }
            throw IllegalArgumentException("use MessageActionViewModel::class.java")
        }
    }

    private val logger = miCore.loggerFactory.create("MessageActionViewModel")

    val text = MutableLiveData<String>()
    val file = MutableLiveData<FileProperty>()

    private val mErrors = MutableStateFlow<Throwable?>(null)
    val errors = mErrors.asStateFlow()

    fun send(){

        val tmpText = text.value
        val tmpFile = file.value
        //text.value = null
        //file.value = null
        viewModelScope.launch(Dispatchers.IO) {
            val createMessage = CreateMessage.Factory.create(messagingId, tmpText, tmpFile?.id)
            runCatching { miCore.getMessageRepository().create(createMessage) }.onFailure {
                logger.error("メッセージ作成中にエラー発生", e = it)
                mErrors.value = it
            }.onSuccess {
                file.postValue(null)
                text.postValue("")
            }
        }
    }

}