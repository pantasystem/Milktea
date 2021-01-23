package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryNoteRepository : NoteRepository{

    private val notes = HashMap<String, Note>()

    private val mutex = Mutex()

    override fun observer(): Flow<NoteRepository.Event> {
        TODO("実装する")
    }

    override suspend fun get(noteId: String): Note? {
        mutex.withLock{
            return notes[noteId]
        }
    }

    /**
     * @param note 追加するノート
     * @return ノートが新たに追加されるとtrue、上書きされた場合はfalseが返されます。
     */
    override suspend fun add(note: Note): Boolean {
        mutex.withLock{
            val n = this.notes[note.id]
            this.notes[note.id] = note
            return n == null
        }
    }

    /**
     * @param noteId 削除するNoteのid
     * @return 実際に削除されるとtrue、そもそも存在していなかった場合にはfalseが返されます
     */
    override suspend fun remove(noteId: String): Boolean {
        mutex.withLock{
            val n = this.notes[noteId]
            this.notes.remove(noteId)
            return n != null
        }
    }
}