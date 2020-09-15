package jp.panta.misskeyandroidclient.model.notes.db

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
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

    override suspend fun add(note: Note): Note {
        notes[ note.id to account.accountId ] = note
        return note
    }

    override suspend fun get(noteId: String): Note? {
        return notes[ noteId to account.accountId ]
    }

    override suspend fun remove(note: Note) {
        notes.remove( note.id to account.accountId )
    }
}