package net.pantasystem.milktea.data.infrastructure.notes.impl

import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.inValues
import io.objectbox.kotlin.toFlow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.notes.impl.db.NoteRecord
import net.pantasystem.milktea.data.infrastructure.notes.impl.db.NoteRecord_
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

class ObjectBoxNoteDataSource @Inject constructor(
    private val boxStore: BoxStore,
    @IODispatcher val coroutineDispatcher: CoroutineDispatcher,
) : NoteDataSource {

    private val noteBox: Box<NoteRecord> by lazy {
        boxStore.boxFor()
    }

    private var listeners = setOf<NoteDataSource.Listener>()
    private val listenersLock = Mutex()

    private val lock = Mutex()
    private var deleteNoteIds = mutableSetOf<Note.Id>()
    private var removedNoteIds = mutableSetOf<Note.Id>()


    override fun addEventListener(listener: NoteDataSource.Listener): Unit = runBlocking {
        listeners = listeners.toMutableSet().apply {
            add(listener)
        }
    }


    override val state: StateFlow<NoteDataSourceState> =
        MutableStateFlow(NoteDataSourceState(emptyMap()))


    override suspend fun getIn(noteIds: List<Note.Id>): Result<List<Note>> =
        runCancellableCatching {
            if (noteIds.isEmpty()) {
                return@runCancellableCatching emptyList()
            }
            withContext(coroutineDispatcher) {
                boxStore.awaitCallInTx {
                    val ids = noteIds.map {
                        NoteRecord.generateAccountIdNoteId(it)
                    }
                    noteBox.query().inValues(
                        NoteRecord_.accountIdAndNoteId,
                        ids.toTypedArray(),
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).build().find().map {
                        it.toModel()
                    }
                } ?: emptyList()
            }
        }

    override suspend fun get(noteId: Note.Id): Result<Note> = runCancellableCatching {
        if (deleteNoteIds.contains(noteId)) {
            throw NoteDeletedException(noteId)
        }
        if (removedNoteIds.contains(noteId)) {
            throw NoteRemovedException(noteId)
        }
        withContext(coroutineDispatcher) {
            boxStore.awaitCallInTx {
                noteBox.query().equal(
                    NoteRecord_.accountIdAndNoteId,
                    NoteRecord.generateAccountIdNoteId(noteId),
                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                ).build().findFirst()?.toModel()
            } ?: throw NoteNotFoundException(noteId)
        }
    }

    override suspend fun exists(noteId: Note.Id): Boolean {
        return noteBox.query().equal(
            NoteRecord_.accountIdAndNoteId,
            NoteRecord.generateAccountIdNoteId(noteId),
            QueryBuilder.StringOrder.CASE_INSENSITIVE
        ).build().findFirst()?.let {
            return true
        } ?: false
    }

    override suspend fun delete(noteId: Note.Id): Result<Boolean> = runCancellableCatching {
        val isDeleted = withContext(coroutineDispatcher) {
            boxStore.awaitCallInTx {
                noteBox.query().equal(
                    NoteRecord_.accountIdAndNoteId,
                    NoteRecord.generateAccountIdNoteId(noteId),
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

    override suspend fun remove(noteId: Note.Id): Result<Boolean> = runCancellableCatching {
        val isRemoved = withContext(coroutineDispatcher) {
            boxStore.awaitCallInTx {
                noteBox.query().equal(
                    NoteRecord_.accountIdAndNoteId,
                    NoteRecord.generateAccountIdNoteId(noteId),
                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                ).build().remove()
            }?.let {
                it > 0
            } ?: false
        }
        lock.withLock {
            removedNoteIds.add(noteId)
        }
        isRemoved
    }

    override suspend fun add(note: Note): Result<AddResult> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            (boxStore.awaitCallInTx {
                val exists = noteBox.query().equal(
                    NoteRecord_.accountIdAndNoteId,
                    NoteRecord.generateAccountIdNoteId(note.id),
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

    override suspend fun addAll(notes: List<Note>): Result<List<AddResult>> =
        runCancellableCatching {
            notes.map {
                add(it)
            }.map {
                it.getOrElse { AddResult.Canceled }
            }
        }

    override suspend fun deleteByUserId(userId: User.Id): Result<Int> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            boxStore.awaitCallInTx {
                noteBox.query()
                    .equal(NoteRecord_.userId, userId.id, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                    .and().equal(NoteRecord_.accountId, userId.accountId)
                    .build().remove().toInt()
            } ?: 0
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeIn(noteIds: List<Note.Id>): Flow<List<Note>> {
        val ids = noteIds.map {
            NoteRecord.generateAccountIdNoteId(it)
        }
        if (ids.isEmpty()) {
            return flowOf(emptyList())
        }
        return noteBox.query().inValues(
            NoteRecord_.accountIdAndNoteId,
            ids.toTypedArray(),
            QueryBuilder.StringOrder.CASE_INSENSITIVE
        ).build().subscribe().toFlow().map { list ->
            list.mapNotNull {
                it?.toModel()
            }
        }.flowOn(coroutineDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeOne(noteId: Note.Id): Flow<Note?> {
        return noteBox.query().equal(
            NoteRecord_.accountIdAndNoteId,
            NoteRecord.generateAccountIdNoteId(noteId),
            QueryBuilder.StringOrder.CASE_INSENSITIVE
        ).build().subscribe().toFlow().map {
            it.firstOrNull()?.toModel()
        }.flowOn(coroutineDispatcher)
    }

    private fun publish(ev: NoteDataSource.Event) = runBlocking {
        listenersLock.withLock {
            listeners.forEach {
                it.on(ev)
            }
        }
    }

    private suspend fun onAdded(note: Note) {
        lock.withLock {
            deleteNoteIds.remove(note.id)
        }
    }
}