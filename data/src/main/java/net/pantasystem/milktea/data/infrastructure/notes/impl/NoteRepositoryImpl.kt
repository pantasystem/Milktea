package net.pantasystem.milktea.data.infrastructure.notes.impl

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.api.misskey.notes.CreateReactionDTO
import net.pantasystem.milktea.api.misskey.notes.DeleteNote
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.drive.FileUploaderProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteCaptureAPIWithAccountProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftNoteDao
import net.pantasystem.milktea.data.infrastructure.notes.onIReacted
import net.pantasystem.milktea.data.infrastructure.notes.onIUnReacted
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.notes.reaction.CreateReaction
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject

@Suppress("UNREACHABLE_CODE", "IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")
class NoteRepositoryImpl @Inject constructor(
    val loggerFactory: Logger.Factory,
    val userDataSource: UserDataSource,
    val noteDataSource: NoteDataSource,
    val filePropertyDataSource: FilePropertyDataSource,
    val encryption: Encryption,
    private val uploader: FileUploaderProvider,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val draftNoteDao: DraftNoteDao,
    val settingStore: SettingStore,
    val getAccount: GetAccount,
    private val noteCaptureAPIProvider: NoteCaptureAPIWithAccountProvider
) : NoteRepository {

    private val logger = loggerFactory.create("NoteRepositoryImpl")
    private val noteDataSourceAdder: NoteDataSourceAdder by lazy {
        NoteDataSourceAdder(userDataSource, noteDataSource, filePropertyDataSource)
    }

    override suspend fun create(createNote: CreateNote): Note {
        val task = PostNoteTask(
            encryption,
            createNote,
            createNote.author,
            loggerFactory,
            filePropertyDataSource
        )
        val result = runCatching {
            task.execute(
                uploader.get(createNote.author)
            ) ?: throw IllegalStateException("ファイルのアップロードに失敗しました")
        }.runCatching {
            this.getOrThrow().let {
                misskeyAPIProvider.get(createNote.author).create(it).body()?.createdNote
            }
        }

        if (result.isFailure) {
            val exDraft = createNote.draftNoteId?.let {
                draftNoteDao.getDraftNote(
                    createNote.author.accountId,
                    createNote.draftNoteId!!
                )
            }
            draftNoteDao.fullInsert(task.toDraftNote(exDraft))
        }
        val noteDTO = result.getOrThrow()
        require(noteDTO != null)
        createNote.draftNoteId?.let {
            draftNoteDao.deleteDraftNote(createNote.author.accountId, draftNoteId = it)
        }

        if (createNote.channelId == null) {
            settingStore.setNoteVisibility(createNote)
        }

        return noteDataSourceAdder.addNoteDtoToDataSource(createNote.author, noteDTO)

    }

    override suspend fun delete(noteId: Note.Id): Boolean {
        val account = getAccount.get(noteId.accountId)
        return misskeyAPIProvider.get(account).delete(
            DeleteNote(i = account.getI(encryption), noteId = noteId.noteId)
        ).isSuccessful
    }

    override suspend fun find(noteId: Note.Id): Note {
        val account = getAccount.get(noteId.accountId)

        var note = try {
            noteDataSource.get(noteId)
        } catch (e: NoteDeletedException) {
            throw e
        } catch (e: Throwable) {
            null
        }

        if (note != null) {
            return note
        }

        logger.debug("request notes/show=$noteId")
        note = try {
            misskeyAPIProvider.get(account).showNote(
                NoteRequest(
                    i = account.getI(encryption),
                    noteId = noteId.noteId
                )
            ).throwIfHasError().body()?.let { resDTO ->
                noteDataSourceAdder.addNoteDtoToDataSource(account, resDTO)
            }
        } catch (e: APIError.NotFoundException) {
            // NOTE(pantasystem): 削除フラグが立つようになり次からNoteDeletedExceptionが投げられる
            noteDataSource.remove(noteId)
            null
        }
        return note ?: throw NoteNotFoundException(noteId)
    }

    override suspend fun findIn(noteIds: List<Note.Id>): List<Note> {
        val notes = noteDataSource.getIn(noteIds)
        val notExistsIds = noteIds.filterNot {
            notes.any { note -> note.id == it }
        }
        if (notExistsIds.isEmpty()) {
            return notes
        }

        fetchIn(notExistsIds)
        return noteDataSource.getIn(noteIds)
    }

    override suspend fun reaction(createReaction: CreateReaction): Boolean {
        val account = getAccount.get(createReaction.noteId.accountId)
        val note = find(createReaction.noteId)

        return runCatching {
            if (postReaction(createReaction) && !noteCaptureAPIProvider.get(account)
                    .isCaptured(createReaction.noteId.noteId)) {
                noteDataSource.add(note.onIReacted(createReaction.reaction))
            }
            true
        }.getOrElse { false }
    }

    override suspend fun unreaction(noteId: Note.Id): Boolean {
        val note = find(noteId)
        val account = getAccount.get(noteId.accountId)
        return postUnReaction(noteId)
                && (noteCaptureAPIProvider.get(account).isCaptured(noteId.noteId)
                || (note.myReaction != null
                && noteDataSource.add(note.onIUnReacted()) != AddResult.CANCEL))
    }

    private suspend fun postReaction(createReaction: CreateReaction): Boolean {
        val account = getAccount.get(createReaction.noteId.accountId)
        val res = misskeyAPIProvider.get(account).createReaction(
            CreateReactionDTO(
                i = account.getI(encryption),
                noteId = createReaction.noteId.noteId,
                reaction = createReaction.reaction
            )
        )
        res.throwIfHasError()
        return res.isSuccessful
    }

    private suspend fun postUnReaction(noteId: Note.Id): Boolean {
        val note = find(noteId)
        val account = getAccount.get(noteId.accountId)
        val res = misskeyAPIProvider.get(account).deleteReaction(
            DeleteNote(
                noteId = note.id.noteId,
                i = account.getI(encryption)
            )
        )
        res.throwIfHasError()
        return res.isSuccessful

    }

    private suspend fun fetchIn(noteIds: List<Note.Id>) {
        val accountMap = noteIds.map {
            it.accountId
        }.distinct().mapNotNull {
            runCatching {
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
                                i = account.getI(encryption)
                            )
                        ).throwIfHasError().body()?.let {
                            noteDataSourceAdder.addNoteDtoToDataSource(account, it)
                        }
                    } catch (e: Throwable) {
                        if (e is APIError.NotFoundException) {
                            noteDataSource.remove(noteId)
                        }
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }
}