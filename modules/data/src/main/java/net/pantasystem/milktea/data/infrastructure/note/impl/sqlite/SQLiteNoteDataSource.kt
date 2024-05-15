package net.pantasystem.milktea.data.infrastructure.note.impl.sqlite

import androidx.room.Transaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.DefaultDispatcher
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteDataSource
import net.pantasystem.milktea.model.note.NoteNotFoundException
import net.pantasystem.milktea.model.note.NoteResult
import net.pantasystem.milktea.model.note.NoteThreadContext
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

class SQLiteNoteDataSource @Inject constructor(
    private val noteDAO: NoteDAO,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher

) : NoteDataSource {

    private var listeners = setOf<NoteDataSource.Listener>()
    private val listenersLock = Mutex()

    private val lock = Mutex()
    private var deleteNoteIds = mutableSetOf<Note.Id>()

    private val clearStorageLock = Mutex()

    override fun addEventListener(listener: NoteDataSource.Listener): Unit = runBlocking {
        listeners = listeners.toMutableSet().apply {
            add(listener)
        }
    }

    override suspend fun getIn(noteIds: List<Note.Id>): Result<List<Note>> =
        runCancellableCatching {
            val ids = noteIds.map {
                NoteEntity.makeEntityId(it)
            }.distinct()
            if (ids.isEmpty()) {
                return@runCancellableCatching emptyList()
            }
            val entities = withContext(ioDispatcher) {
                noteDAO.getIn(ids)
            }
            withContext(defaultDispatcher) {
                entities.map {
                    it.toModel()
                }
            }
        }

    override suspend fun get(noteId: Note.Id): Result<Note> = runCancellableCatching {
        if (deleteNoteIds.contains(noteId)) {
            return Result.failure(Exception("Note is deleted"))
        }
        val entity = withContext(ioDispatcher) {
            noteDAO.get(NoteEntity.makeEntityId(noteId))
        }
        entity?.toModel() ?: throw NoteNotFoundException(noteId)
    }

    override suspend fun getWithState(noteId: Note.Id): Result<NoteResult> =
        runCancellableCatching {
            if (deleteNoteIds.contains(noteId)) {
                return@runCancellableCatching NoteResult.Deleted
            }
            val entity = withContext(ioDispatcher) {
                noteDAO.get(NoteEntity.makeEntityId(noteId))
            }
            when (entity) {
                null -> NoteResult.NotFound
                else -> NoteResult.Success(entity.toModel())
            }
        }

    override suspend fun findByReplyId(id: Note.Id): Result<List<Note>> = runCancellableCatching {
        val entities = withContext(ioDispatcher) {
            noteDAO.findByReplyId(id.accountId, id.noteId)
        }
        withContext(defaultDispatcher) {
            entities.map {
                it.toModel()
            }
        }
    }

    override suspend fun exists(noteId: Note.Id): Boolean {
        return runCancellableCatching {
            val entity = withContext(ioDispatcher) {
                noteDAO.get(NoteEntity.makeEntityId(noteId))
            }
            entity != null
        }.getOrDefault(false)
    }

    override suspend fun delete(noteId: Note.Id): Result<Boolean> = runCancellableCatching {
        val entityId = NoteEntity.makeEntityId(noteId)
        val count = withContext(ioDispatcher) {
            noteDAO.count(entityId)
        }
        withContext(ioDispatcher) {
            noteDAO.delete(entityId)
        }
        lock.withLock {
            deleteNoteIds.add(noteId)
        }
        if (count > 0) {
            publish(NoteDataSource.Event.Deleted(noteId))
        }

        count > 0
    }

    @Transaction
    override suspend fun add(note: Note): Result<AddResult> = runCancellableCatching {
        val dbId = NoteEntity.makeEntityId(note.id)
        val relationEntity = NoteWithRelation.fromModel(note)
        // exists check
        val existsEntity = withContext(ioDispatcher) {
            noteDAO.get(relationEntity.note.id)
        }
        val entity = relationEntity.note
        withContext(ioDispatcher) {
            if (existsEntity == null) {
                noteDAO.insert(entity)
            } else {
                noteDAO.update(entity)
            }
        }
        val needInsert = existsEntity == null
        withContext(ioDispatcher) {
            when (note.type) {
                is Note.Type.Mastodon -> {
                    if (needInsert) {
                        relationEntity.mastodonMentions?.let {
                            noteDAO.insertMastodonMentions(
                                it
                            )
                        }
                        relationEntity.mastodonTags?.let {
                            noteDAO.insertMastodonTags(
                                it
                            )
                        }
                    }
                }

                is Note.Type.Misskey -> Unit
            }

            if (!needInsert) {
                // detach
                noteDAO.deletePollChoicesByNoteId(dbId)
                noteDAO.deleteCustomEmojisByNoteId(dbId)
                noteDAO.deleteReactionCountsByNoteId(dbId)
            } else {
                relationEntity.noteFiles?.let {
                    noteDAO.insertNoteFiles(it)
                }
                relationEntity.visibleUserIds?.let {
                    noteDAO.insertVisibleIds(it)
                }
            }

            relationEntity.reactionCounts?.let {
                noteDAO.insertReactionCounts(it)
            }

            relationEntity.pollChoices?.let {
                noteDAO.insertPollChoices(it)
            }

            relationEntity.customEmojis?.let {
                noteDAO.insertCustomEmojis(it)
            }

        }

        if (existsEntity == null) AddResult.Created else AddResult.Updated
    }

    override suspend fun addAll(notes: List<Note>): Result<List<AddResult>> =
        runCancellableCatching {
            notes.mapNotNull {
                add(it).getOrNull()
            }
        }

    override suspend fun clear(): Result<Unit> {
        return runCancellableCatching {
            clearStorageLock.withLock {
                withContext(ioDispatcher) {
                    noteDAO.clear()
                }
            }
        }
    }

    override suspend fun addNoteThreadContext(
        noteId: Note.Id,
        context: NoteThreadContext
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun clearNoteThreadContext(noteId: Note.Id): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun findNoteThreadContext(noteId: Note.Id): Result<NoteThreadContext> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteByUserId(userId: User.Id): Result<Int> = runCancellableCatching {
        val count = withContext(ioDispatcher) {
            noteDAO.deleteByUserId(userId.accountId, userId.id)
        }
        count
    }

    override fun observeIn(noteIds: List<Note.Id>): Flow<List<Note>> {
        val ids = noteIds.map {
            NoteEntity.makeEntityId(it)
        }.distinct()
        if (ids.isEmpty()) {
            return flowOf(emptyList())
        }
        return noteDAO.observeByIds(ids).flowOn(ioDispatcher).map { notes ->
            notes.map {
                it.toModel()
            }
        }.flowOn(defaultDispatcher)
    }

    override fun observeOne(noteId: Note.Id): Flow<Note?> {
        return noteDAO.observeById(NoteEntity.makeEntityId(noteId)).flowOn(ioDispatcher).map {
            it?.toModel()
        }
    }

    override fun observeNoteThreadContext(noteId: Note.Id): Flow<NoteThreadContext?> {
        TODO("Not yet implemented")
    }

    override suspend fun findLocalCount(): Result<Long> = runCancellableCatching {
        withContext(ioDispatcher) {
            noteDAO.count()
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