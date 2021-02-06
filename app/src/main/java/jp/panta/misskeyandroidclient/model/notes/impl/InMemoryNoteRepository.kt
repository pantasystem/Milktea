package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteCapture
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryNoteRepository: NoteRepository{

    private val notes = HashMap<Note.Id, Note>()

    private val mutex = Mutex()

    @ExperimentalCoroutinesApi
    private val eventBroadcastChannel = BroadcastChannel<NoteRepository.Event>(1)

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun observer(): Flow<NoteRepository.Event> {
        return eventBroadcastChannel.asFlow()
    }

    override suspend fun get(noteId: Note.Id): Note? {
        mutex.withLock{
            return notes[noteId]
        }
    }

    /**
     * @param note 追加するノート
     * @return ノートが新たに追加されるとtrue、上書きされた場合はfalseが返されます。
     */
    @ExperimentalCoroutinesApi
    override suspend fun add(note: Note): AddResult {
        mutex.withLock{
            val n = this.notes[note.id]
            if(n != null && n.instanceUpdatedAt != note.instanceUpdatedAt){
                return AddResult.CANCEL
            }
            this.notes[note.id] = note
            note.updated()
            eventBroadcastChannel.send(NoteRepository.Event.Added(note.id))

            return if(n == null){
                AddResult.CREATED
            } else AddResult.UPDATED
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun addAll(notes: List<Note>): List<AddResult> {
        return notes.map{
            this.add(it)
        }
    }

    /**
     * @param noteId 削除するNoteのid
     * @return 実際に削除されるとtrue、そもそも存在していなかった場合にはfalseが返されます
     */
    @ExperimentalCoroutinesApi
    override suspend fun remove(noteId: Note.Id): Boolean {
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

    @ExperimentalCoroutinesApi
    override suspend fun removeByUserId(userId: User.Id): Int {
        return mutex.withLock {
            notes.values.filter {
                it.userId == userId
            }.count {
                this.remove(it.id)
            }
        }
    }
}