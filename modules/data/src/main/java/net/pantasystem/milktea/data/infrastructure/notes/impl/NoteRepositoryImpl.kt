package net.pantasystem.milktea.data.infrastructure.notes.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.notes.GetNoteChildrenRequest
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.notes.CreateNote
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.NoteDeletedException
import net.pantasystem.milktea.model.notes.NoteNotFoundException
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.NoteResult
import net.pantasystem.milktea.model.notes.NoteState
import net.pantasystem.milktea.model.notes.NoteThreadContext
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.notes.poll.Vote
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    val loggerFactory: Logger.Factory,
    val userDataSource: UserDataSource,
    val noteDataSource: NoteDataSource,
    val filePropertyDataSource: FilePropertyDataSource,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val mastodonAPIProvider: MastodonAPIProvider,
    val noteDataSourceAdder: NoteDataSourceAdder,
    val getAccount: GetAccount,
    private val noteApiAdapterFactory: NoteApiAdapter.Factory,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : NoteRepository {

    private val logger = loggerFactory.create("NoteRepositoryImpl")


    override suspend fun create(createNote: CreateNote): Result<Note> = runCancellableCatching {
        withContext(ioDispatcher) {
            convertAndAdd(createNote.author, noteApiAdapterFactory.create(createNote.author).create(createNote))
        }
    }

    override suspend fun renote(noteId: Note.Id): Result<Note> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            when (account.instanceType) {
                Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                    val n = find(noteId).getOrThrow()
                    create(
                        CreateNote(
                            author = account, renoteId = noteId,
                            text = null,
                            visibility = n.visibility
                        )
                    ).getOrThrow()
                }
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    val toot = mastodonAPIProvider.get(account).reblog(noteId.noteId)
                        .throwIfHasError()
                        .body()
                    noteDataSourceAdder.addTootStatusDtoIntoDataSource(
                        account,
                        requireNotNull(toot)
                    )
                }
            }
        }
    }

    override suspend fun unrenote(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(noteId.accountId)
            when (account.instanceType) {
                Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> delete(noteId).getOrThrow()
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    val res = mastodonAPIProvider.get(account).unreblog(noteId.noteId)
                        .throwIfHasError()
                        .body()
                    noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, requireNotNull(res))
                }

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

            logger.debug("request notes/show=$noteId")
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
                when (val type = note.type) {
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
            when (account.instanceType) {
                Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
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
                    noteDataSource.clearNoteThreadContext(noteId)
                    noteDataSource.addNoteThreadContext(noteId, NoteThreadContext(
                        ancestors = ancestors,
                        descendants = emptyList()
                    ))
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
                    noteDataSource.clearNoteThreadContext(noteId)
                    noteDataSource.addNoteThreadContext(noteId, NoteThreadContext(
                        ancestors = ancestors,
                        descendants = descendants
                    ))
                }
            }
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
                when (account.instanceType) {
                    Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                        misskeyAPIProvider.get(account.normalizedInstanceUri).noteState(
                            NoteRequest(
                                i = account.token,
                                noteId = noteId.noteId
                            )
                        ).throwIfHasError().body()!!.let {
                            NoteState(
                                isFavorited = it.isFavorited,
                                isMutedThread = it.isMutedThread,
                                isWatching = when (val watching = it.isWatching) {
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

    private suspend fun getMisskeyDescendants(targetNoteId: Note.Id): List<NoteDTO> {
        val account = getAccount.get(targetNoteId.accountId)
        return requireNotNull(
            misskeyAPIProvider.get(account).children(
                GetNoteChildrenRequest(
                    i = account.token,
                    noteId = targetNoteId.noteId,
                    limit = 30,
                    depth = 2,
                )
            ).throwIfHasError().body()
        )
    }

    private suspend fun syncRecursiveThreadContext4Misskey(
        targetNoteId: Note.Id,
        appendTo: Note.Id,
    ) {
        val account = getAccount.get(appendTo.accountId)
        val descendants = getMisskeyDescendants(targetNoteId).map {
            noteDataSourceAdder.addNoteDtoToDataSource(account, it)
        }
        val threadContext = noteDataSource.findNoteThreadContext(targetNoteId).getOrThrow()
        noteDataSource.addNoteThreadContext(
            targetNoteId,
            threadContext.copy(
                descendants = threadContext.descendants + descendants
            )
        )
        coroutineScope {
            descendants.map { note ->
                async {
                    syncRecursiveThreadContext4Misskey(note.id, appendTo)
                }
            }.awaitAll()
        }
    }
}