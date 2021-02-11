package jp.panta.misskeyandroidclient.streaming.notes


interface NoteSubscriberProvider {

    fun get(accountId: Long) : NoteSubscriber?
}