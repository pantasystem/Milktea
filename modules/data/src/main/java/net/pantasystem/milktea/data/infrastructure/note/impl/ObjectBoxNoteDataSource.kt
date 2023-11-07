package net.pantasystem.milktea.data.infrastructure.note.impl

import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.inValues
import io.objectbox.kotlin.toFlow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.DefaultDispatcher
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.note.impl.db.NoteRecord
import net.pantasystem.milktea.data.infrastructure.note.impl.db.NoteRecord_
import net.pantasystem.milktea.data.infrastructure.note.impl.db.NoteThreadRecordDAO
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.note.*
import net.pantasystem.milktea.model.user.User
import java.util.*
import javax.inject.Inject

class ObjectBoxNoteDataSource @Inject constructor(
    private val boxStore: BoxStore,
    private val noteThreadRecordDAO: NoteThreadRecordDAO,
    @IODispatcher val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher val defaultDispatcher: CoroutineDispatcher,
    loggerFactory: Logger.Factory
) : NoteDataSource {

    private val logger by lazy {
        loggerFactory.create("ObjectBoxNoteDS")
    }

    private val noteBox: Box<NoteRecord> by lazy {
        boxStore.boxFor()
    }

    private var listeners = setOf<NoteDataSource.Listener>()
    private val listenersLock = Mutex()

    private val lock = Mutex()
    private var deleteNoteIds = mutableSetOf<Note.Id>()

    private val changedIdFlow = MutableStateFlow<String>("")


    override fun addEventListener(listener: NoteDataSource.Listener): Unit = runBlocking {
        listeners = listeners.toMutableSet().apply {
            add(listener)
        }
    }


    override suspend fun getIn(
        noteIds: List<Note.Id>
    ): Result<List<Note>> = runCancellableCatching {
        if (noteIds.isEmpty()) {
            return@runCancellableCatching emptyList()
        }
        withContext(ioDispatcher) {
            val ids = noteIds.map {
                NoteRecord.generateAccountAndNoteId(it)
            }
            noteBox.query().inValues(
                NoteRecord_.accountIdAndNoteId,
                ids.toTypedArray(),
                QueryBuilder.StringOrder.CASE_INSENSITIVE
            ).build().find().map {
                it.toModel()
            }
        }
    }

    override suspend fun get(noteId: Note.Id): Result<Note> = runCancellableCatching {
        if (deleteNoteIds.contains(noteId)) {
            throw NoteDeletedException(noteId)
        }
        withContext(ioDispatcher) {
            noteBox.query().equal(
                NoteRecord_.accountIdAndNoteId,
                NoteRecord.generateAccountAndNoteId(noteId),
                QueryBuilder.StringOrder.CASE_INSENSITIVE
            ).build().findFirst()?.toModel() ?: throw NoteNotFoundException(noteId)
        }
    }

    override suspend fun getWithState(
        noteId: Note.Id
    ): Result<NoteResult> = runCancellableCatching {
        if (deleteNoteIds.contains(noteId)) {
            return@runCancellableCatching NoteResult.Deleted
        }
        val note = withContext(ioDispatcher) {
            noteBox.query().equal(
                NoteRecord_.accountIdAndNoteId,
                NoteRecord.generateAccountAndNoteId(noteId),
                QueryBuilder.StringOrder.CASE_INSENSITIVE
            ).build().findFirst()?.toModel()
        }
        when (note) {
            null -> NoteResult.NotFound
            else -> NoteResult.Success(note)
        }
    }

    override suspend fun findByReplyId(id: Note.Id): Result<List<Note>> = runCancellableCatching {
        withContext(ioDispatcher) {
            noteBox.query().equal(
                NoteRecord_.replyId,
                id.noteId,
                QueryBuilder.StringOrder.CASE_INSENSITIVE,
            ).and().equal(
                NoteRecord_.accountId,
                id.accountId
            ).build().find().mapNotNull {
                it?.toModel()
            }
        }
    }

    override suspend fun exists(noteId: Note.Id): Boolean {
        return noteBox.query().equal(
            NoteRecord_.accountIdAndNoteId,
            NoteRecord.generateAccountAndNoteId(noteId),
            QueryBuilder.StringOrder.CASE_INSENSITIVE
        ).build().findFirst()?.let {
            return true
        } ?: false
    }

    override suspend fun delete(noteId: Note.Id): Result<Boolean> = runCancellableCatching {
        val isDeleted = withContext(ioDispatcher) {
            boxStore.awaitCallInTx {
                noteBox.query().equal(
                    NoteRecord_.accountIdAndNoteId,
                    NoteRecord.generateAccountAndNoteId(noteId),
                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                ).build().remove()
            }?.let {
                it > 0
            } ?: false
        }
        lock.withLock {
            deleteNoteIds.add(noteId)
        }
        if (isDeleted) {
            publish(NoteDataSource.Event.Deleted(noteId))
        }
        isDeleted
    }


    override suspend fun add(note: Note): Result<AddResult> = runCancellableCatching {
        withContext(ioDispatcher) {
            (boxStore.awaitCallInTx {
                val exists = noteBox.query().equal(
                    NoteRecord_.accountIdAndNoteId,
                    NoteRecord.generateAccountAndNoteId(note.id),
                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                ).build().findFirst()
                if (exists == null) {
                    noteBox.put(NoteRecord.from(note))
                    AddResult.Created
                } else {
                    exists.applyModel(note)
                    noteBox.put(exists)
                    AddResult.Updated
                }
            } ?: AddResult.Canceled)
        }.also {
            when (it) {
                AddResult.Canceled -> Unit
                AddResult.Created -> {
                    onAdded(note)
                    publish(NoteDataSource.Event.Created(note.id, note))
                }

                AddResult.Updated -> publish(NoteDataSource.Event.Updated(note.id, note))
            }
        }


    }

    override suspend fun addAll(
        notes: List<Note>
    ): Result<List<AddResult>> = runCancellableCatching {
        withContext(ioDispatcher) {
            boxStore.awaitCallInTx {
                val existsNotes = noteBox.query().inValues(
                    NoteRecord_.accountIdAndNoteId,
                    notes.map {
                        NoteRecord.generateAccountAndNoteId(it.id)
                    }.toTypedArray(),
                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                ).build().find().associateBy {
                    it.noteId
                }
                val willSave = notes.map {
                    val exists = existsNotes[it.id.noteId]
                    if (exists == null) {
                        NoteRecord.from(it) to AddResult.Created
                    } else {
                        exists.applyModel(it)
                        exists to AddResult.Updated
                    }
                }
                noteBox.put(willSave.map {
                    it.first
                })
                willSave.map {
                    it.second
                }
            }
        } ?: emptyList()
    }

    override suspend fun deleteByUserId(userId: User.Id): Result<Int> = runCancellableCatching {
        withContext(ioDispatcher) {
            boxStore.awaitCallInTx {
                noteBox.query()
                    .equal(NoteRecord_.userId, userId.id, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                    .and().equal(NoteRecord_.accountId, userId.accountId)
                    .build().remove().toInt()
            } ?: 0
        }
    }

    override suspend fun clear(): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            noteBox.removeAll()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeIn(noteIds: List<Note.Id>): Flow<List<Note>> {
        val ids = noteIds.map {
            NoteRecord.generateAccountAndNoteId(it)
        }
        if (ids.isEmpty()) {
            return flowOf(emptyList())
        }
        return noteBox.query().inValues(
            NoteRecord_.accountIdAndNoteId,
            ids.toTypedArray(),
            QueryBuilder.StringOrder.CASE_INSENSITIVE
        ).build().subscribe().toFlow().flowOn(ioDispatcher).map { list ->
            list.mapNotNull {
                it?.toModel()
            }
        }.flowOn(defaultDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeOne(noteId: Note.Id): Flow<Note?> {
        return noteBox.query().equal(
            NoteRecord_.accountIdAndNoteId,
            NoteRecord.generateAccountAndNoteId(noteId),
            QueryBuilder.StringOrder.CASE_INSENSITIVE
        ).build().subscribe().toFlow().flowOn(ioDispatcher).map {
            it.firstOrNull()?.toModel()
        }.flowOn(defaultDispatcher).catch {
            logger.error("Note observeエラー", it)
        }
    }

    @OptIn(FlowPreview::class)
    override fun observeNoteThreadContext(noteId: Note.Id): Flow<NoteThreadContext?> {
        return suspend {
            noteThreadRecordDAO.appendBlank(noteId)
        }.asFlow().map { record ->
            NoteThreadContext(
                descendants = record.descendants.map {
                    it.toModel()
                },
                ancestors = record.ancestors.map {
                    it.toModel()
                }
            )
        }
    }

    override suspend fun addNoteThreadContext(
        noteId: Note.Id,
        context: NoteThreadContext
    ): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            noteThreadRecordDAO.clearRelation(noteId)
            val record = noteThreadRecordDAO.appendBlank(noteId)

            record.ancestors.clear()
            record.ancestors.addAll(findByNotes(context.ancestors.map { it.id }))
            record.descendants.clear()
            record.descendants.addAll(findByNotes(context.descendants.map { it.id }))
            noteThreadRecordDAO.update(record)
        }
    }

    override suspend fun clearNoteThreadContext(
        noteId: Note.Id
    ): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            noteThreadRecordDAO.clearRelation(noteId)
        }
    }

    override suspend fun findNoteThreadContext(
        noteId: Note.Id
    ): Result<NoteThreadContext> = runCancellableCatching {
        withContext(ioDispatcher) {
            noteThreadRecordDAO.findBy(noteId)?.let { record ->
                NoteThreadContext(
                    ancestors = record.ancestors.mapNotNull {
                        it?.toModel()
                    },
                    descendants = record.descendants.mapNotNull {
                        it?.toModel()
                    }
                )
            } ?: NoteThreadContext(emptyList(), emptyList())
        }
    }

    override suspend fun findLocalCount(): Result<Long> = runCancellableCatching {
        withContext(ioDispatcher) {
            noteBox.count()
        }
    }

    private fun publish(ev: NoteDataSource.Event) = runBlocking {
        listenersLock.withLock {
            listeners.forEach {
                it.on(ev)
            }
        }
        changedIdFlow.value = UUID.randomUUID().toString()
    }


    private suspend fun onAdded(note: Note) {
        lock.withLock {
            deleteNoteIds.remove(note.id)
        }
    }

    private fun findByNotes(noteIds: List<Note.Id>): List<NoteRecord> {
        return noteBox.query().inValues(
            NoteRecord_.accountIdAndNoteId, noteIds.map {
                NoteRecord.generateAccountAndNoteId(it)
            }.toTypedArray(),
            QueryBuilder.StringOrder.CASE_INSENSITIVE
        ).build().find()
    }
}