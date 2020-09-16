package jp.panta.misskeyandroidclient.model.notes

import io.reactivex.Observable
import jp.panta.misskeyandroidclient.model.account.Account
import java.util.*

interface NoteEventStore {
    val account: Account

    fun release(event: NoteEvent)

    fun getEventStream(date: Date = Date()): Observable<NoteEvent>
}