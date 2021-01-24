package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryNoteRepository : NoteRepository{

    private val notes = HashMap<String, Note>()

    private val mutex = Mutex()

    @ExperimentalCoroutinesApi
    private val eventBroadcastChannel = BroadcastChannel<NoteRepository.Event>(1)

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun observer(): Flow<NoteRepository.Event> {
        return eventBroadcastChannel.asFlow()
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
    @ExperimentalCoroutinesApi
    override suspend fun add(note: Note): NoteRepository.AddResult {
        mutex.withLock{
            val n = this.notes[note.id]
            if(n != null && n.instanceUpdatedAt != note.instanceUpdatedAt){
                return NoteRepository.AddResult.CANCEL
            }
            this.notes[note.id] = note
            note.updated()
            eventBroadcastChannel.send(NoteRepository.Event.Added(note))

            return if(n == null) NoteRepository.AddResult.CREATED else NoteRepository.AddResult.UPDATED
        }
    }

    /**
     * @param noteId 削除するNoteのid
     * @return 実際に削除されるとtrue、そもそも存在していなかった場合にはfalseが返されます
     */
    @ExperimentalCoroutinesApi
    override suspend fun remove(noteId: String): Boolean {
        mutex.withLock{
            val n = this.notes[noteId]
            this.notes.remove(noteId)
            if(n == null){
                return false
            }
            eventBroadcastChannel.send(NoteRepository.Event.Deleted(noteId = noteId))
            return true
        }
    }
}