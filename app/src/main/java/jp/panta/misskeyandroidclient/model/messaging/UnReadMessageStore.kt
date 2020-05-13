package jp.panta.misskeyandroidclient.model.messaging

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.core.Account

/**
 * アカウント１に対して１
 */
class UnReadMessageStore(val account: Account) {

    private val mUnreadMessagesMap = HashMap<MessagingId, HashSet<Message>>()
    private val mUnreadMessagesLiveDataMap = HashMap<MessagingId, MutableLiveData<List<Message>>>()

    fun addUnReadMessage(message: Message){
        val messagingId = message.messagingId(account)
        val unreadMessageSet: HashSet<Message>
        synchronized(mUnreadMessagesMap){
            var unreadMessages = mUnreadMessagesMap[messagingId]
            if( unreadMessages == null ){
                unreadMessages = HashSet()
                mUnreadMessagesMap[messagingId] = unreadMessages
            }
            unreadMessages.add(message)
            unreadMessageSet = unreadMessages
        }

        updateLiveData(messagingId, unreadMessageSet)
    }

    private fun updateLiveData(messagingId: MessagingId, messages: Set<Message>): MutableLiveData<List<Message>>{
        synchronized(mUnreadMessagesLiveDataMap){
            var liveData = mUnreadMessagesLiveDataMap[messagingId]
            if( liveData == null ){
                liveData = MutableLiveData()
                mUnreadMessagesLiveDataMap[messagingId] = liveData
            }

            liveData.postValue(messages.sortedBy {
                it.createdAt
            })
            return liveData
        }
    }



    fun getUnreadMessages(messagingId: MessagingId): List<Message>{
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


    fun getUnreadMessagesLiveData(messagingId: MessagingId): LiveData<List<Message>>{
        synchronized(mUnreadMessagesLiveDataMap){
            var liveData = mUnreadMessagesLiveDataMap[messagingId]
            if(liveData == null){
                liveData = updateLiveData(messagingId, getUnreadMessages(messagingId).toHashSet())
            }
            return liveData
        }
    }
    fun allUnReadMessagesCount(): Int{
        synchronized(mUnreadMessagesMap){
            return mUnreadMessagesMap.values.sumBy {
                it.size
            }
        }
    }

    fun readMessage(message: Message){
        val messagingId = message.messagingId(account)
        synchronized(mUnreadMessagesMap){
            mUnreadMessagesMap[messagingId]?.apply{
                remove(message)
                updateLiveData(messagingId, this)
            }
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
            updateLiveData(messagingId, emptySet())
        }
    }

}