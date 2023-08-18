package net.pantasystem.milktea.data.infrastructure.notes.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.MemoryCacheCleaner
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteDataSource
import net.pantasystem.milktea.model.note.NoteDeletedException
import net.pantasystem.milktea.model.note.NoteNotFoundException
import net.pantasystem.milktea.model.note.NoteResult
import net.pantasystem.milktea.model.note.NoteThreadContext
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

class InMemoryNoteDataSource @Inject constructor(
    memoryCacheCleaner: MemoryCacheCleaner
): NoteDataSource, MemoryCacheCleaner.Cleanable {


    private var notes = mapOf<Note.Id, Note>()

    private val mutex = Mutex()
    private val listenersLock = Mutex()

    private var listeners = setOf<NoteDataSource.Listener>()

    private val _state = MutableStateFlow(NoteDataSourceState(emptyMap()))

    private var deleteNoteIds = mutableSetOf<Note.Id>()

    init {
        addEventListener {
            _state.update {
                it.copy(map = notes)
            }
        }
        memoryCacheCleaner.register(this)
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

    override suspend fun exists(noteId: Note.Id): Boolean {
        return notes[noteId] != null
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
        }.distinctUntilChanged()
    }


    override suspend fun findByReplyId(id: Note.Id): Result<List<Note>> {
        return Result.success(
            _state.value.map.values.filter {
                it.replyId == id
            }
        )
    }

    override fun observeOne(noteId: Note.Id): Flow<Note?> {
        return _state.map {
            it.getOrNull(noteId)
        }.distinctUntilChanged()
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

    override suspend fun clean() {
        mutex.withLock {
            notes = emptyMap()
        }
    }

    override suspend fun clear(): Result<Unit> = runCancellableCatching {
        mutex.withLock {
            notes = emptyMap()
        }
    }

    override suspend fun addNoteThreadContext(
        noteId: Note.Id,
        context: NoteThreadContext
    ): Result<Unit> = Result.success(Unit)

    override fun observeNoteThreadContext(noteId: Note.Id): Flow<NoteThreadContext?> {
        return emptyFlow()
    }

    override suspend fun findNoteThreadContext(noteId: Note.Id): Result<NoteThreadContext> = Result.success(
        NoteThreadContext(emptyList(), emptyList())
    )

    override suspend fun clearNoteThreadContext(noteId: Note.Id): Result<Unit> = Result.success(Unit)

    private fun publish(ev: NoteDataSource.Event) = runBlocking {
        listenersLock.withLock {
            listeners.forEach {
                it.on(ev)
            }
        }
    }

    override suspend fun findLocalCount(): Result<Long> {
        return Result.success(notes.size.toLong())
    }

    override suspend fun getWithState(noteId: Note.Id): Result<NoteResult> = runCancellableCatching {
        if (deleteNoteIds.contains(noteId)) {
            return@runCancellableCatching NoteResult.Deleted
        }

        when(val note = notes[noteId]) {
            null -> NoteResult.NotFound
            else -> NoteResult.Success(note)
        }
    }

}

data class NoteDataSourceState(
    val map: Map<Note.Id, Note>,
) {
    fun findIn(ids: List<Note.Id>): List<Note> {
        return ids.mapNotNull {
            map[it]
        }
    }

    fun getOrNull(id: Note.Id): Note? {
        return map[id]
    }
}
