package net.pantasystem.milktea.data.infrastructure.notes.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.api.misskey.notes.CreateReactionDTO
import net.pantasystem.milktea.api.misskey.notes.DeleteNote
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.api.misskey.notes.mute.ToggleThreadMuteRequest
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.drive.FileUploaderProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteCaptureAPIWithAccountProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.notes.onIReacted
import net.pantasystem.milktea.data.infrastructure.notes.onIUnReacted
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.notes.poll.Vote
import net.pantasystem.milktea.model.notes.reaction.CreateReaction
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject

@Suppress("UNREACHABLE_CODE", "IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")
class NoteRepositoryImpl @Inject constructor(
    val loggerFactory: Logger.Factory,
    val userDataSource: UserDataSource,
    val noteDataSource: NoteDataSource,
    val filePropertyDataSource: FilePropertyDataSource,
    private val uploader: FileUploaderProvider,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val getAccount: GetAccount,
    private val noteCaptureAPIProvider: NoteCaptureAPIWithAccountProvider,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : NoteRepository {

    private val logger = loggerFactory.create("NoteRepositoryImpl")
    private val noteDataSourceAdder: NoteDataSourceAdder by lazy {
        NoteDataSourceAdder(userDataSource, noteDataSource, filePropertyDataSource)
    }

    override suspend fun create(createNote: CreateNote): Result<Note> = runCancellableCatching {
        withContext(ioDispatcher) {
            val task = PostNoteTask(
                createNote,
                createNote.author,
                loggerFactory,
                filePropertyDataSource
            )
            val result = runCancellableCatching {
                task.execute(
                    uploader.get(createNote.author)
                ) ?: throw IllegalStateException("ファイルのアップロードに失敗しました")
            }.mapCancellableCatching {
                misskeyAPIProvider.get(createNote.author).create(it).throwIfHasError()
                    .body()?.createdNote
            }.onFailure {
                logger.error("create note error", it)
            }

            val noteDTO = result.getOrThrow()
            require(noteDTO != null)
            noteDataSourceAdder.addNoteDtoToDataSource(createNote.author, noteDTO)
        }
    }

    override suspend fun delete(noteId: Note.Id): Result<Unit> = runCancellableCatching{
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            misskeyAPIProvider.get(account).delete(
                DeleteNote(i = account.token, noteId = noteId.noteId)
            ).throwIfHasError()
        }
    }

    override suspend fun find(noteId: Note.Id): Result<Note> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)

            var note = try {
                noteDataSource.get(noteId).getOrThrow()
            } catch (e: NoteDeletedException) {
                throw e
            } catch (e: Throwable) {
                null
            }

            if (note != null) {
                return@withContext note
            }

            logger.debug("request notes/show=$noteId")
            note = try {
                misskeyAPIProvider.get(account).showNote(
                    NoteRequest(
                        i = account.token,
                        noteId = noteId.noteId
                    )
                ).throwIfHasError().body()?.let { resDTO ->
                    noteDataSourceAdder.addNoteDtoToDataSource(account, resDTO)
                }
            } catch (e: APIError.NotFoundException) {
                // NOTE(pantasystem): 削除フラグが立つようになり次からNoteDeletedExceptionが投げられる
                noteDataSource.delete(noteId)
                null
            }
            note ?: throw NoteNotFoundException(noteId)
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

            val notExistsAndNoteDeletedNoteIds = notExistsIds.filterNot { noteId ->
                noteDataSource.get(noteId).fold(
                    onSuccess = { true },
                    onFailure = {
                        // NOTE: 削除済みとキャッシュ上からも削除済みのケースの場合はFetchしない。
                        // NOTE: Fetchしない理由としてはうっかりバグが発生してAPIに過剰にリクエストを送信してしまう可能性があるから
                        it is NoteDeletedException || it is NoteRemovedException
                    }
                )
            }

            fetchIn(notExistsAndNoteDeletedNoteIds)
            noteDataSource.getIn(noteIds).getOrThrow()
        }
    }

    override suspend fun reaction(createReaction: CreateReaction): Result<Boolean> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(createReaction.noteId.accountId)
            val note = find(createReaction.noteId).getOrThrow()

            runCancellableCatching {
                if (postReaction(createReaction) && !noteCaptureAPIProvider.get(account)
                        .isCaptured(createReaction.noteId.noteId)
                ) {
                    noteDataSource.add(note.onIReacted(createReaction.reaction))
                }
                true
            }.getOrElse { e ->
                if (e is APIError.ClientException) {
                    return@getOrElse false
                }
                throw e
            }
        }
    }

    override suspend fun unreaction(noteId: Note.Id): Result<Boolean> = runCancellableCatching {
        withContext(ioDispatcher) {
            val note = find(noteId).getOrThrow()
            val account = getAccount.get(noteId.accountId)
            postUnReaction(noteId)
                    && (noteCaptureAPIProvider.get(account).isCaptured(noteId.noteId)
                    || (note.myReaction != null
                    && noteDataSource.add(note.onIUnReacted()).getOrThrow() != AddResult.Canceled))
        }
    }


    override suspend fun vote(noteId: Note.Id, choice: Poll.Choice): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            misskeyAPIProvider.get(account).vote(
                Vote(
                    i = getAccount.get(noteId.accountId).token,
                    choice = choice.index,
                    noteId = noteId.noteId
                )
            ).throwIfHasError()
        }
    }

    private suspend fun postReaction(createReaction: CreateReaction): Boolean {
        val account = getAccount.get(createReaction.noteId.accountId)
        val res = misskeyAPIProvider.get(account).createReaction(
            CreateReactionDTO(
                i = account.token,
                noteId = createReaction.noteId.noteId,
                reaction = createReaction.reaction
            )
        )
        res.throwIfHasError()
        return res.isSuccessful
    }

    private suspend fun postUnReaction(noteId: Note.Id): Boolean {
        val note = find(noteId).getOrThrow()
        val account = getAccount.get(noteId.accountId)
        val res = misskeyAPIProvider.get(account).deleteReaction(
            DeleteNote(
                noteId = note.id.noteId,
                i = account.token
            )
        )
        res.throwIfHasError()
        return res.isSuccessful

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
                        misskeyAPIProvider.get(account).showNote(
                            NoteRequest(
                                i = account.token,
                                noteId = noteId.noteId,
                            )
                        ).throwIfHasError().body()?.let {
                            noteDataSourceAdder.addNoteDtoToDataSource(account, it)
                        }
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

    override suspend fun syncChildren(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            val dtoList = misskeyAPIProvider.get(account).children(
                NoteRequest(
                    i = account.token,
                    noteId = noteId.noteId,
                    limit = 100,
                )
            ).throwIfHasError().body()!!
            dtoList.map {
                noteDataSourceAdder.addNoteDtoToDataSource(account, it)
            }
        }
    }

    override suspend fun syncConversation(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            val dtoList = misskeyAPIProvider.get(account).conversation(
                NoteRequest(
                    i = account.token,
                    noteId = noteId.noteId,

                    )
            ).throwIfHasError().body()!!
            dtoList.map {
                noteDataSourceAdder.addNoteDtoToDataSource(account, it)
            }
        }
    }

    override suspend fun sync(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            val note = misskeyAPIProvider.get(account).showNote(
                NoteRequest(
                    i = account.token,
                    noteId = noteId.noteId
                )
            ).throwIfHasError().body()!!
            noteDataSourceAdder.addNoteDtoToDataSource(account, note)
        }
    }

    override suspend fun createThreadMute(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            misskeyAPIProvider.get(account).createThreadMute(
                ToggleThreadMuteRequest(
                    i = account.token,
                    noteId = noteId.noteId
                )
            ).throwIfHasError()
        }
    }

    override suspend fun deleteThreadMute(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            misskeyAPIProvider.get(account).deleteThreadMute(
                ToggleThreadMuteRequest(
                    i = account.token,
                    noteId = noteId.noteId,
                )
            ).throwIfHasError()
        }
    }

    override suspend fun findNoteState(noteId: Note.Id): Result<NoteState> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            misskeyAPIProvider.get(account.normalizedInstanceDomain).noteState(
                NoteRequest(
                    i = account.token,
                    noteId = noteId.noteId
                )
            ).throwIfHasError().body()!!.let {
                NoteState(
                    isFavorited = it.isFavorited,
                    isMutedThread = it.isMutedThread,
                    isWatching = when(val watching = it.isWatching) {
                        null -> NoteState.Watching.None
                        else -> NoteState.Watching.Some(watching)
                    }
                )
            }
        }
    }

    override fun observeIn(noteIds: List<Note.Id>): Flow<List<Note>> {
        return noteDataSource.observeIn(noteIds)
    }

    override fun observeOne(noteId: Note.Id): Flow<Note?> {
        return noteDataSource.observeOne(noteId)
    }
}