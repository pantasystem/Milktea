package net.pantasystem.milktea.data.infrastructure.notes.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.notes.impl.db.NoteThreadRecordDAO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.notes.poll.Vote
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject

@Suppress("UNREACHABLE_CODE", "IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")
class NoteRepositoryImpl @Inject constructor(
    val loggerFactory: Logger.Factory,
    val userDataSource: UserDataSource,
    val noteDataSource: NoteDataSource,
    val filePropertyDataSource: FilePropertyDataSource,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val mastodonAPIProvider: MastodonAPIProvider,
    val noteDataSourceAdder: NoteDataSourceAdder,
    val getAccount: GetAccount,
    private val noteApiAdapter: NoteApiAdapter,
    private val noteThreadRecordDAO: NoteThreadRecordDAO,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : NoteRepository {

    private val logger = loggerFactory.create("NoteRepositoryImpl")


    override suspend fun create(createNote: CreateNote): Result<Note> = runCancellableCatching {
        withContext(ioDispatcher) {
            convertAndAdd(createNote.author, noteApiAdapter.create(createNote))
        }
    }

    override suspend fun renote(noteId: Note.Id): Result<Note> = runCancellableCatching{
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            when(account.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    val n = find(noteId).getOrThrow()
                    create(CreateNote(
                        author = account, renoteId = noteId,
                        text = null,
                        visibility = n.visibility
                    )).getOrThrow()
                }
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    val toot = mastodonAPIProvider.get(account).reblog(noteId.noteId)
                        .throwIfHasError()
                        .body()
                    noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, requireNotNull(toot))
                }
            }
        }
    }

    override suspend fun unrenote(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            when(account.instanceType) {
                Account.InstanceType.MISSKEY -> delete(noteId).getOrThrow()
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    val res = mastodonAPIProvider.get(account).unreblog(noteId.noteId)
                        .throwIfHasError()
                        .body()
                    noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, requireNotNull(res))
                }

            }
        }
    }

    override suspend fun delete(noteId: Note.Id): Result<Note> = runCancellableCatching{
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            val note = find(noteId).getOrThrow()
            when(val result = noteApiAdapter.delete(noteId)) {
                is DeleteNoteResultType.Mastodon -> noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, result.status)
                DeleteNoteResultType.Misskey -> note
            }
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
                convertAndAdd(account, noteApiAdapter.showNote(noteId))
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


    override suspend fun vote(noteId: Note.Id, choice: Poll.Choice): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            val note = find(noteId).getOrThrow()
            when(val type = note.type) {
                is Note.Type.Mastodon -> {
                    mastodonAPIProvider.get(account).voteOnPoll(
                        requireNotNull(type.pollId),
                        choices = listOf(choice.index)
                    )
                }
                is Note.Type.Misskey -> {
                    misskeyAPIProvider.get(account).vote(
                        Vote(
                            i = getAccount.get(noteId.accountId).token,
                            choice = choice.index,
                            noteId = noteId.noteId
                        )
                    ).throwIfHasError()
                }
            }

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
                        convertAndAdd(account, noteApiAdapter.showNote(noteId))
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
            when(account.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    val ancestors = requireNotNull(
                        misskeyAPIProvider.get(account).conversation(
                            NoteRequest(
                                i = account.token,
                                noteId = noteId.noteId,
                            )
                        ).throwIfHasError().body()
                    ).map {
                        noteDataSourceAdder.addNoteDtoToDataSource(account, it)
                    }
                    noteThreadRecordDAO.clearRelation(noteId)
                    noteThreadRecordDAO.appendAncestors(noteId, ancestors.map { it.id })
                    syncRecursiveThreadContext4Misskey(noteId, noteId)
                }
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    val body = requireNotNull(
                        mastodonAPIProvider.get(account).getStatusesContext(noteId.noteId)
                            .throwIfHasError()
                            .body()
                    )
                    val ancestors = body.ancestors.map {
                        noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, it)
                    }

                    val descendants = body.descendants.map {
                        noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, it)
                    }
                    noteThreadRecordDAO.clearRelation(noteId)
                    noteThreadRecordDAO.appendAncestors(noteId, ancestors.map { it.id })
                    noteThreadRecordDAO.appendDescendants(noteId, descendants.map { it.id })
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    override fun observeThreadContext(noteId: Note.Id): Flow<NoteThreadContext> {
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

    override suspend fun sync(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            convertAndAdd(account, noteApiAdapter.showNote(noteId))
        }
    }

    override suspend fun createThreadMute(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            when(val result = noteApiAdapter.createThreadMute(noteId)) {
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
            when(val result = noteApiAdapter.deleteThreadMute(noteId)) {
                is ToggleThreadMuteResultType.Mastodon -> {
                    noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, result.status)
                }
                ToggleThreadMuteResultType.Misskey -> Unit
            }
        }
    }

    override suspend fun findNoteState(noteId: Note.Id): Result<NoteState> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            when(account.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    misskeyAPIProvider.get(account.normalizedInstanceUri).noteState(
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
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    find(noteId).mapCancellableCatching {
                        NoteState(
                            isFavorited = (it.type as Note.Type.Mastodon).favorited ?: false,
                            isMutedThread = (it.type as Note.Type.Mastodon).muted ?: false,
                            isWatching = NoteState.Watching.None,
                        )
                    }.getOrThrow()
                }
            }

        }
    }

    override fun observeIn(noteIds: List<Note.Id>): Flow<List<Note>> {
        return noteDataSource.observeIn(noteIds)
    }

    override fun observeOne(noteId: Note.Id): Flow<Note?> {
        return noteDataSource.observeOne(noteId)
    }

    private suspend fun convertAndAdd(account: Account, type: NoteResultType): Note {
        return when(type) {
            is NoteResultType.Mastodon -> noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, type.status)
            is NoteResultType.Misskey -> noteDataSourceAdder.addNoteDtoToDataSource(account, type.note)
        }
    }

    private suspend fun getMisskeyDescendants(targetNoteId: Note.Id): List<NoteDTO> {
        val account = getAccount.get(targetNoteId.accountId)
        return requireNotNull(misskeyAPIProvider.get(account).children(NoteRequest(
            i = account.token,
            noteId = targetNoteId.noteId
        )).throwIfHasError().body())
    }

    private suspend fun syncRecursiveThreadContext4Misskey(targetNoteId: Note.Id, appendTo: Note.Id) {
        val account = getAccount.get(appendTo.accountId)
        val descendants = getMisskeyDescendants(targetNoteId).map {
            noteDataSourceAdder.addNoteDtoToDataSource(account, it)
        }
        noteThreadRecordDAO.appendDescendants(appendTo, descendants.map { it.id })
        coroutineScope {
            descendants.map { note ->
                async {
                    syncRecursiveThreadContext4Misskey(note.id, appendTo)
                }
            }.awaitAll()
        }
    }
}