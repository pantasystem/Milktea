package net.pantasystem.milktea.data.model.notes.impl

import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteNotFoundException
import net.pantasystem.milktea.model.notes.NoteDataSource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.Logger
import javax.inject.Inject

class InMemoryNoteDataSource @Inject constructor(
    loggerFactory: Logger.Factory
): NoteDataSource {

    val logger = loggerFactory.create("InMemoryNoteRepository")

    private val notes = HashMap<Note.Id, Note>()

    private val mutex = Mutex()
    private val listenersLock = Mutex()

    private var listeners = setOf<NoteDataSource.Listener>()

    override fun addEventListener(listener: NoteDataSource.Listener): Unit = runBlocking {
        listeners = listeners.toMutableSet().apply {
            add(listener)
        }
    }


    override suspend fun get(noteId: Note.Id): Note {
        mutex.withLock{
            return notes[noteId]
                ?: throw NoteNotFoundException(noteId)
        }
    }

    /**
     * @param note 追加するノート
     * @return ノートが新たに追加されるとtrue、上書きされた場合はfalseが返されます。
     */
    override suspend fun add(note: Note): AddResult {
       return createOrUpdate(note).also {
           if(it == AddResult.CREATED) {
               publish(NoteDataSource.Event.Created(note.id, note))
           }else if(it == AddResult.UPDATED) {
               publish(NoteDataSource.Event.Updated(note.id, note))
           }
       }
    }

    override suspend fun addAll(notes: List<Note>): List<AddResult> {
        return notes.map{
            this.add(it)
        }
    }

    /**
     * @param noteId 削除するNoteのid
     * @return 実際に削除されるとtrue、そもそも存在していなかった場合にはfalseが返されます
     */
    override suspend fun remove(noteId: Note.Id): Boolean {
        suspend fun delete(noteId: Note.Id): Boolean {
            mutex.withLock{
                val n = this.notes[noteId]
                this.notes.remove(noteId)
                return n != null
            }
        }

        return delete(noteId).also {
            if(it) {
                publish(NoteDataSource.Event.Deleted(noteId))
            }
        }

    }

    override suspend fun removeByUserId(userId: net.pantasystem.milktea.model.user.User.Id): Int {
        val result = mutex.withLock {
            notes.values.filter {
                it.userId == userId
            }.mapNotNull {
                if(this.remove(it.id)) {
                    it
                }else null
            }
        }
        return result.map {
            remove(it.id)
        }.count ()
    }

    private suspend fun createOrUpdate(note: Note): AddResult {
        mutex.withLock{
            val n = this.notes[note.id]
            if(n != null && n.instanceUpdatedAt > note.instanceUpdatedAt){
                return AddResult.CANCEL
            }
            this.notes[note.id] = note
            note.updated()

            return if(n == null){
                AddResult.CREATED
            } else {
                AddResult.UPDATED
            }
        }
    }

    private fun publish(ev: NoteDataSource.Event) = runBlocking {
        listenersLock.withLock {
            listeners.forEach {
                it.on(ev)
            }
        }
    }

}