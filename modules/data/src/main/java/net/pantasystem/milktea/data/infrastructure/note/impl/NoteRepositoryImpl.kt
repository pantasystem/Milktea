package net.pantasystem.milktea.data.infrastructure.note.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.note.NoteDataSourceAdder
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.note.CreateNote
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteDataSource
import net.pantasystem.milktea.model.note.NoteDeletedException
import net.pantasystem.milktea.model.note.NoteNotFoundException
import net.pantasystem.milktea.model.note.NoteRepository
import net.pantasystem.milktea.model.note.NoteResult
import net.pantasystem.milktea.model.note.NoteState
import net.pantasystem.milktea.model.note.NoteThreadContext
import net.pantasystem.milktea.model.note.poll.Poll
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    val noteDataSource: NoteDataSource,
    val noteDataSourceAdder: NoteDataSourceAdder,
    val getAccount: GetAccount,
    private val noteApiAdapterFactory: NoteApiAdapter.Factory,
    private val threadContextApiAdapterFactory: ThreadContextApiAdapter.Factory,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : NoteRepository {


    override suspend fun create(createNote: CreateNote): Result<Note> = runCancellableCatching {
        withContext(ioDispatcher) {
            convertAndAdd(createNote.author, noteApiAdapterFactory.create(createNote.author).create(createNote))
        }
    }

    override suspend fun renote(noteId: Note.Id, inChannel: Boolean): Result<Note> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            val n = find(noteId).getOrThrow()
            convertAndAdd(account, noteApiAdapterFactory.create(account).renote(n, inChannel))
        }
    }

    override suspend fun unrenote(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            when(val result = noteApiAdapterFactory.create(account).unrenote(noteId)) {
                is UnrenoteResultType.Mastodon -> {
                    noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, requireNotNull(result.status))
                }
                UnrenoteResultType.Misskey -> Unit
            }
        }
    }

    override suspend fun delete(noteId: Note.Id): Result<Note> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            val note = find(noteId).getOrThrow()
            when (val result = noteApiAdapterFactory.create(account).delete(noteId)) {
                is DeleteNoteResultType.Mastodon -> noteDataSourceAdder.addTootStatusDtoIntoDataSource(
                    account,
                    result.status
                )
                DeleteNoteResultType.Misskey -> note
            }
        }
    }

    override suspend fun find(noteId: Note.Id): Result<Note> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)

            when(val state = noteDataSource.getWithState(noteId).getOrThrow()) {
                NoteResult.Deleted -> throw NoteDeletedException(noteId)
                is NoteResult.Success -> return@withContext state.note
                NoteResult.NotFound -> Unit
            }

            val note = try {
                convertAndAdd(account, noteApiAdapterFactory.create(account).showNote(noteId))
            } catch (e: APIError.NotFoundException) {
                // NOTE(pantasystem): 削除フラグが立つようになり次からNoteDeletedExceptionが投げられる
                noteDataSource.delete(noteId)
                throw NoteNotFoundException(noteId)
            }
            note
        }
    }

    override suspend fun findIn(noteIds: List<Note.Id>): List<Note> {
        return withContext(ioDispatcher) {
            val notes = noteDataSource.getIn(noteIds).getOrThrow()
            val notExistsIds = noteIds.filterNot {
                notes.any { note -> note.id == it }
            }
            if (notExistsIds.isEmpty()) {
                return@withContext notes
            }

            val notExistsAndNoteDeletedNoteIds = notExistsIds.filter { noteId ->
                when(noteDataSource.getWithState(noteId).getOrThrow()) {
                    NoteResult.Deleted -> false
                    is NoteResult.Success -> false
                    NoteResult.NotFound -> true
                }
            }

            fetchIn(notExistsAndNoteDeletedNoteIds)
            noteDataSource.getIn(noteIds).getOrThrow()
        }
    }


    override suspend fun vote(noteId: Note.Id, choice: Poll.Choice): Result<Unit> =
        runCancellableCatching {
            withContext(ioDispatcher) {
                val account = getAccount.get(noteId.accountId)
                val note = find(noteId).getOrThrow()
                noteApiAdapterFactory.create(account).vote(noteId, choice, note)
            }
        }


    private suspend fun fetchIn(noteIds: List<Note.Id>) {
        val accountMap = noteIds.map {
            it.accountId
        }.distinct().mapNotNull {
            runCancellableCatching {
                getAccount.get(it)
            }.getOrNull()
        }.associateBy {
            it.accountId
        }

        coroutineScope {
            noteIds.map { noteId ->
                async {
                    try {
                        val account = accountMap.getValue(noteId.accountId)
                        convertAndAdd(account, noteApiAdapterFactory.create(account).showNote(noteId))
                    } catch (e: Throwable) {
                        if (e is APIError.NotFoundException) {
                            noteDataSource.delete(noteId)
                        }
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    override suspend fun syncThreadContext(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            threadContextApiAdapterFactory.create(account).syncThreadContext(noteId)
        }
    }

    override fun observeThreadContext(noteId: Note.Id): Flow<NoteThreadContext> {
        return noteDataSource.observeNoteThreadContext(noteId).filterNotNull()
    }

    override suspend fun sync(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            convertAndAdd(account, noteApiAdapterFactory.create(account).showNote(noteId))
        }
    }

    override suspend fun createThreadMute(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            when (val result = noteApiAdapterFactory.create(account).createThreadMute(noteId)) {
                is ToggleThreadMuteResultType.Mastodon -> {
                    noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, result.status)
                }
                ToggleThreadMuteResultType.Misskey -> Unit
            }
        }
    }

    override suspend fun deleteThreadMute(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            when (val result = noteApiAdapterFactory.create(account).deleteThreadMute(noteId)) {
                is ToggleThreadMuteResultType.Mastodon -> {
                    noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, result.status)
                }
                ToggleThreadMuteResultType.Misskey -> Unit
            }
        }
    }

    override suspend fun findNoteState(noteId: Note.Id): Result<NoteState> =
        runCancellableCatching {
            withContext(ioDispatcher) {
                val account = getAccount.get(noteId.accountId)
                val target = find(noteId).getOrThrow()
                noteApiAdapterFactory.create(account).findNoteState(target)
            }
        }

    override fun observeIn(noteIds: List<Note.Id>): Flow<List<Note>> {
        return noteDataSource.observeIn(noteIds)
    }

    override fun observeOne(noteId: Note.Id): Flow<Note?> {
        return noteDataSource.observeOne(noteId)
    }

    private suspend fun convertAndAdd(account: Account, type: NoteResultType): Note {
        return when (type) {
            is NoteResultType.Mastodon -> noteDataSourceAdder.addTootStatusDtoIntoDataSource(
                account,
                type.status
            )
            is NoteResultType.Misskey -> noteDataSourceAdder.addNoteDtoToDataSource(
                account,
                type.note
            )
        }
    }

}