package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.impl.InMemoryNoteEventStore
import java.util.concurrent.ConcurrentHashMap
@Deprecated("NoteRepository, NoteCaptureAPI, NoteCaptureAPIAdapterに置き換える予定のため非推奨")
class NoteEventStoreFactory {

    private val accountAndStore = ConcurrentHashMap<Long, NoteEventStore>()

    fun create(account: Account) : NoteEventStore{
        synchronized(this){
            var store = accountAndStore[account.accountId]
            if(store == null){
                store = InMemoryNoteEventStore(account)
                accountAndStore[account.accountId] = store
            }
            return store
        }
    }
}