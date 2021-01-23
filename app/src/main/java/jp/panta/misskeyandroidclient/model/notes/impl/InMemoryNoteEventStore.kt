package jp.panta.misskeyandroidclient.model.notes.impl

import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureEvent
import jp.panta.misskeyandroidclient.model.notes.NoteEventStore
import java.util.*

class InMemoryNoteEventStore(override val account: Account) : NoteEventStore{

    private val subject = ReplaySubject.create<NoteCaptureEvent>()

    override fun release(event: NoteCaptureEvent) {
        subject.onNext(event)
    }

    override fun getEventStream(date: Date): Observable<NoteCaptureEvent> {
        return subject.filter{
            it.eventAt >= date
        }
    }

}