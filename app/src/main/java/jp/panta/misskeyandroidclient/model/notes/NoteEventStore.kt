package jp.panta.misskeyandroidclient.model.notes

import io.reactivex.Observable
import jp.panta.misskeyandroidclient.model.account.Account
import java.util.*

@Deprecated("NoteRepository, NoteCaptureAPI, NoteCaptureAPIAdapterに置き換える予定のため非推奨")
interface NoteEventStore {
    val account: Account

    fun release(event: NoteCaptureEvent)

    fun getEventStream(date: Date = Date()): Observable<NoteCaptureEvent>
}