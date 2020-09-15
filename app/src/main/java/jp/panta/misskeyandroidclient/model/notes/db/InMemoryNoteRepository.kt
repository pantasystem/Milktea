package jp.panta.misskeyandroidclient.model.notes.db

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.streming.note.v2.NoteEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryNoteRepository(override val account: Account) : NoteRepository{

    class Factory : NoteRepository.Factory{

        private val repositories = ConcurrentHashMap<Long, NoteRepository>()

        override fun create(account: Account): NoteRepository {
            return synchronized(this){
                var repository = repositories[account.accountId]
                if(repository == null){
                    repository = InMemoryNoteRepository(account)
                    repositories[account.accountId] = repository
                }
                repository
            }

        }
    }

    val notes = ConcurrentHashMap<Pair<String, Long>, Note>()

    private val subject = ReplaySubject.create<NoteRepository.Event>()

    override suspend fun add(note: Note): Note {
        val key = note.id to account.accountId
        val exNote = notes[ key ]
        notes[ key ] = note
        if(exNote == null){
            subject.onNext(NoteRepository.Event(note, NoteRepository.Event.Type.CREATED, Date()))
        }else{
            subject.onNext(NoteRepository.Event(note, NoteRepository.Event.Type.UPDATED, Date()))
        }
        return note
    }

    override suspend fun get(noteId: String): Note? {
        return notes[ noteId to account.accountId ]
    }

    override suspend fun remove(note: Note) {
        if(notes.remove( note.id to account.accountId ) != null ){
            subject.onNext(NoteRepository.Event(note, NoteRepository.Event.Type.DELETED, Date()))
        }
    }

    override fun getEventStream(date: Date): Observable<NoteRepository.Event> {
        return subject.filter {
            it.createdAt >= date
        }
    }
}