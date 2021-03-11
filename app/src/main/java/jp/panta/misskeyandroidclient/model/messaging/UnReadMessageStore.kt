package jp.panta.misskeyandroidclient.model.messaging

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.model.account.Account

/**
 * アカウント１に対して１
 */
@Deprecated("使いにくいので廃止")
class UnReadMessageStore(val account: Account) {

    private val mUnreadMessagesMap = HashMap<MessagingId, HashSet<MessageDTO>>()
    private val mUnreadMessagesLiveDataMap = HashMap<MessagingId, MutableLiveData<List<MessageDTO>>>()

    private val mMessageIdMessagingIdMap = HashMap<String, MessagingId>()


    private val mUnreadMessagesCount = MutableLiveData<Int>()

    fun addUnReadMessage(message: MessageDTO){
        val messagingId = message.messagingId(account)
        synchronized(mUnreadMessagesMap){
            var unreadMessages = mUnreadMessagesMap[messagingId]
            if( unreadMessages == null ){
                unreadMessages = HashSet()
                mUnreadMessagesMap[messagingId] = unreadMessages
            }
            unreadMessages.add(message)
        }
        synchronized(mMessageIdMessagingIdMap){
            mMessageIdMessagingIdMap[message.id] = messagingId
        }

        updateLiveData(messagingId)
    }

    private fun updateLiveData(messagingId: MessagingId): MutableLiveData<List<MessageDTO>>{
        synchronized(mUnreadMessagesLiveDataMap){
            var liveData = mUnreadMessagesLiveDataMap[messagingId]
            if( liveData == null ){
                liveData = MutableLiveData()
                mUnreadMessagesLiveDataMap[messagingId] = liveData
            }

            val list = getUnreadMessages(messagingId)
            updateUnreadMessageCount()
            val before = liveData.value
            if(list != before){
                liveData.postValue(list)
            }

            Log.d("UnReadMessageStore", "liveDataを更新しました 総未読数:${list.size} 更新データ:$list")
            return liveData
        }
    }



    fun getUnreadMessages(messagingId: MessagingId): List<MessageDTO>{
        synchronized(mUnreadMessagesMap){
            var messages = mUnreadMessagesMap[messagingId]
            if( messages == null ){
                messages = HashSet()
                mUnreadMessagesMap[messagingId] = messages
            }
            return messages.sortedBy {
                it.createdAt
            }
        }
    }

    private fun updateUnreadMessageCount(){
        synchronized(mUnreadMessagesMap){
            val sum = mUnreadMessagesMap.values.sumBy {
                it.size
            }
            mUnreadMessagesCount.postValue(sum)
        }
    }


    fun getUnreadMessagesLiveData(messagingId: MessagingId): LiveData<List<MessageDTO>>{
        return updateLiveData(messagingId)
    }
    fun allUnReadMessagesCount(): Int{
        synchronized(mUnreadMessagesMap){
            return mUnreadMessagesMap.values.sumBy {
                it.size
            }
        }
    }

    fun readMessage(message: MessageDTO){
        val messagingId = message.messagingId(account)
        synchronized(mUnreadMessagesMap){
            mUnreadMessagesMap[messagingId]?.apply{
                remove(message)
            }
            updateLiveData(messagingId)
        }
    }

    fun readAll(){
        synchronized(mUnreadMessagesMap){
            mUnreadMessagesMap.keys.forEach{ id ->
                readAll(id)
            }
        }
    }

    fun readAll(messagingId: MessagingId){
        synchronized(mUnreadMessagesMap){
            mUnreadMessagesMap.remove(messagingId)
            updateLiveData(messagingId)
        }
    }

    fun read(messageId: String){
        val messagingId= synchronized(mMessageIdMessagingIdMap){
            mMessageIdMessagingIdMap[messageId]
        }?: return
        synchronized(mUnreadMessagesMap){
            val messages = mUnreadMessagesMap[messagingId]?: return
            val iterator = messages.iterator()
            while(iterator.hasNext()){
                if(iterator.next().id == messageId){
                    iterator.remove()
                }
            }
            updateLiveData(messagingId)
        }
    }

    fun readAll(messageIds: List<String>){
        messageIds.forEach{
            read(it)
        }
    }

    fun readSameAllMessages(messageId: String){
        val messagingId = synchronized(mMessageIdMessagingIdMap){
            mMessageIdMessagingIdMap[messageId]
        }?: return
        readAll(messagingId)
    }

    fun getUnreadMessageCountLiveData(): LiveData<Int>{
        return mUnreadMessagesCount
    }

}