package jp.panta.misskeyandroidclient.viewmodel.messaging

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.MessageAction
import retrofit2.Call
import java.lang.IllegalArgumentException
import retrofit2.Callback
import retrofit2.Response
import jp.panta.misskeyandroidclient.model.account.Account

class MessageActionViewModel(
    val account: Account,
    val misskeyAPI: MisskeyAPI,
    private val messageHistory: Message,
    private val encryption: Encryption
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val account: Account,
        val miApplication: MiApplication,
        private val messageHistory: Message
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass == MessageActionViewModel::class.java){
                return MessageActionViewModel(account, miApplication.getMisskeyAPI(account), messageHistory, miApplication.getEncryption()) as T
            }
            throw IllegalArgumentException("use MessageActionViewModel::class.java")
        }
    }


    val text = MutableLiveData<String>()
    val file = MutableLiveData<FileProperty>()

    fun send(){
        val factory = MessageAction.Factory(account, messageHistory)
        val action = factory.actionCreateMessage(text.value, file.value?.id, encryption)
        val tmpText = text.value
        val tmpFile = file.value
        text.value = null
        file.value = null
        misskeyAPI.createMessage(action).enqueue(object : Callback<Message>{
            override fun onResponse(call: Call<Message>, response: Response<Message>) {
                if (response.code() != 200) {
                    file.postValue(tmpFile)
                    text.postValue(tmpText)
                }
            }
            override fun onFailure(call: Call<Message>, t: Throwable) {
                Log.d("MessageActionViewModel", "失敗しました", t)
                file.postValue(tmpFile)
                text.postValue(tmpText)
            }
        })
    }

}