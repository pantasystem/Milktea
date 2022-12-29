package net.pantasystem.milktea.data.infrastructure.notes.impl

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

class InMemoryNoteDataSource @Inject constructor(): NoteDataSource {


    private var notes = mapOf<Note.Id, Note>()

    private val mutex = Mutex()
    private val listenersLock = Mutex()

    private var listeners = setOf<NoteDataSource.Listener>()

    private val _state = MutableStateFlow(NoteDataSourceState(emptyMap()))

    private var deleteNoteIds = mutableSetOf<Note.Id>()
    private var removedNoteIds = mutableSetOf<Note.Id>()

    override val state: StateFlow<NoteDataSourceState>
        get() = _state

    init {
        addEventListener {
            _state.update {
                it.copy(notes)
            }
        }
    }

    override fun addEventListener(listener: NoteDataSource.Listener): Unit = runBlocking {
        listeners = listeners.toMutableSet().apply {
            add(listener)
        }
    }


    override suspend fun get(noteId: Note.Id): Result<Note> = runCancellableCatching {
        mutex.withLock{
            if (deleteNoteIds.contains(noteId)) {
                throw NoteDeletedException(noteId)
            }
            if (removedNoteIds.contains(noteId)) {
                throw NoteRemovedException(noteId)
            }
            notes[noteId]
                ?: throw NoteNotFoundException(noteId)
        }
    }

    override suspend fun getIn(noteIds: List<Note.Id>): Result<List<Note>> = runCancellableCatching {
        noteIds.mapNotNull { noteId ->
            notes[noteId]
        }
    }

    /**
     * @param note 追加するノート
     * @return ノートが新たに追加されるとtrue、上書きされた場合はfalseが返されます。
     */
    override suspend fun add(note: Note): Result<AddResult> = runCancellableCatching {
       createOrUpdate(note).also {
           if(it == AddResult.Created) {
               publish(NoteDataSource.Event.Created(note.id, note))
           }else if(it == AddResult.Updated) {
               publish(NoteDataSource.Event.Updated(note.id, note))
           }
       }
    }

    override suspend fun addAll(notes: List<Note>): Result<List<AddResult>> = runCancellableCatching {
        notes.map{
            this.add(it).getOrElse {
                AddResult.Canceled
            }
        }
    }

    override suspend fun remove(noteId: Note.Id): Result<Boolean> = runCancellableCatching {
        mutex.withLock{
            val n = this.notes[noteId]
            notes = notes - noteId
            removedNoteIds.add(noteId)
            n != null
        }
    }
    /**
     * @param noteId 削除するNoteのid
     * @return 実際に削除されるとtrue、そもそも存在していなかった場合にはfalseが返されます
     */
    override suspend fun delete(noteId: Note.Id): Result<Boolean> = runCancellableCatching {
        suspend fun delete(noteId: Note.Id): Boolean {
            mutex.withLock{
                val n = this.notes[noteId]
                notes = notes - noteId
                deleteNoteIds.add(noteId)
                return n != null
            }
        }

        delete(noteId).also {
            if(it) {
                publish(NoteDataSource.Event.Deleted(noteId))
            }
        }

    }

    override suspend fun deleteByUserId(userId: User.Id): Result<Int> = runCancellableCatching {
        val result = mutex.withLock {
            notes.values.filter {
                it.userId == userId
            }.mapNotNull {
                if(this.delete(it.id).getOrThrow()) {
                    it
                }else null
            }
        }
        result.mapNotNull {
            delete(it.id).getOrNull()
        }.count ()
    }

    override fun observeIn(noteIds: List<Note.Id>): Flow<List<Note>> {
        return _state.map { state ->
            noteIds.mapNotNull {
                state.getOrNull(it)
            }
        }
    }

    override fun observeOne(noteId: Note.Id): Flow<Note?> {
        return _state.map {
            it.getOrNull(noteId)
        }
    }

    private suspend fun createOrUpdate(note: Note): AddResult {
        mutex.withLock{
            val n = this.notes[note.id]
            notes = notes + (note.id to note)

            deleteNoteIds.remove(note.id)
            return if(n == null){
                AddResult.Created
            } else {
                AddResult.Updated
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