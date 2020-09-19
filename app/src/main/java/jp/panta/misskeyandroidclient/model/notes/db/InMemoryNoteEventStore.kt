package jp.panta.misskeyandroidclient.model.notes.db

import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.NoteEvent
import jp.panta.misskeyandroidclient.model.notes.NoteEventStore
import java.util.*

class InMemoryNoteEventStore(override val account: Account) : NoteEventStore{

    private val subject = ReplaySubject.create<NoteEvent>()

    override fun release(event: NoteEvent) {
        subject.onNext(event)
    }

    override fun getEventStream(date: Date): Observable<NoteEvent> {
        return subject.filter{
            it.eventAt >= date
        }
    }

}