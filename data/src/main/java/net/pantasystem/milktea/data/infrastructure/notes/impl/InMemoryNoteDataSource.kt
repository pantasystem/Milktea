package net.pantasystem.milktea.data.infrastructure.notes.impl

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

class InMemoryNoteDataSource @Inject constructor(
    loggerFactory: Logger.Factory
): NoteDataSource {

    val logger = loggerFactory.create("InMemoryNoteRepository")

    private val notes = HashMap<Note.Id, Note>()

    private val mutex = Mutex()
    private val listenersLock = Mutex()

    private var listeners = setOf<NoteDataSource.Listener>()

    private val _state = MutableStateFlow(NoteDataSourceState(emptyMap()))

    private var deleteNoteIds = mutableSetOf<Note.Id>()

    override val state: StateFlow<NoteDataSourceState>
        get() = _state

    init {
        addEventListener {
            _state.update {
                it.copy(notes.toMutableMap())
            }
        }
    }

    override fun addEventListener(listener: NoteDataSource.Listener): Unit = runBlocking {
        listeners = listeners.toMutableSet().apply {
            add(listener)
        }
    }


    override suspend fun get(noteId: Note.Id): Note {
        mutex.withLock{
            if (deleteNoteIds.contains(noteId)) {
                throw NoteDeletedException(noteId)
            }
            return notes[noteId]
                ?: throw NoteNotFoundException(noteId)
        }
    }

    override suspend fun getIn(noteIds: List<Note.Id>): List<Note> {
        mutex.withLock {
            return noteIds.mapNotNull { noteId ->
                notes[noteId]
            }
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
                deleteNoteIds.add(noteId)
                return n != null
            }
        }

        return delete(noteId).also {
            if(it) {
                publish(NoteDataSource.Event.Deleted(noteId))
            }
        }

    }

    override suspend fun removeByUserId(userId: User.Id): Int {
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

            deleteNoteIds.remove(note.id)
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