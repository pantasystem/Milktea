package jp.panta.misskeyandroidclient.model.messaging

class UnReadMessageStore(val messagingId: MessagingId) {

    private val unreadMessages = HashSet<Message>()

    fun addUnReadMessage(message: Message){
        unreadMessages.add(message)
    }

    fun getUnreadMessages(): List<Message>{
        return unreadMessages.toList().sortedBy {
            it.createdAt
        }
    }

    fun readMessage(message: Message){
        unreadMessages.remove(message)
    }

    fun readAll(){
        unreadMessages.clear()
    }
}