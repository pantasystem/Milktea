package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.api.notes.DeleteNote
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.drive.FileUploader
import jp.panta.misskeyandroidclient.model.drive.OkHttpDriveFileUploader
import jp.panta.misskeyandroidclient.model.notes.CreateNote
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteNotFoundException
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notes.reaction.CreateReaction
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.PostNoteTask
import java.lang.IllegalStateException

class NoteRepositoryImpl(
    val miCore: MiCore
) : NoteRepository {

    override suspend fun create(createNote: CreateNote): Note {
        val task = PostNoteTask(miCore.getEncryption(), createNote, createNote.author, miCore.loggerFactory)
        val result = runCatching {task.execute(
                miCore.createFileUploader(createNote.author)
            )?: throw IllegalStateException("ファイルのアップロードに失敗しました")
        }.runCatching {
            getOrThrow().let {
                miCore.getMisskeyAPI(createNote.author).create(it).execute().body()?.createdNote
            }
        }

        if(result.isFailure) {
            val exDraft = createNote.draftNoteId?.let{ miCore.getDraftNoteDAO().getDraftNote(createNote.author.accountId, createNote.draftNoteId) }
            miCore.getDraftNoteDAO().fullInsert(task.toDraftNote(exDraft))
        }
        val noteDTO = result.getOrThrow()
        require(noteDTO != null)
        createNote.draftNoteId?.let{
            miCore.getDraftNoteDAO().deleteDraftNote(createNote.author.accountId, draftNoteId = it)
        }
        return miCore.getGetters().noteRelationGetter.get(createNote.author, noteDTO).note

    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun delete(noteId: Note.Id): Boolean {
        val account = miCore.getAccountRepository().get(noteId.accountId)
        return miCore.getMisskeyAPI(account).delete(
            DeleteNote(i = account.getI(miCore.getEncryption()), noteId = noteId.noteId)
        ).execute().isSuccessful
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun find(noteId: Note.Id): Note {
        val account = miCore.getAccount(noteId.accountId)

        var note = runCatching {
            miCore.getNoteDataSource().get(noteId)
        }.getOrNull()
        if(note != null) {
            return note
        }

        note = miCore.getMisskeyAPI(account).showNote(NoteRequest(
            i = account.getI(miCore.getEncryption()),
            noteId = noteId.noteId
        )).execute()?.body()?.let{
            miCore.getGetters().noteRelationGetter.get(account, it).note
        }
        note?: throw NoteNotFoundException(noteId)
        return note
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun reaction(createReaction: CreateReaction): Boolean {
        val account = miCore.getAccount(createReaction.noteId.accountId)
        val note = find(createReaction.noteId)
        if(note.myReaction != null) {
            miCore.getMisskeyAPI(account).deleteReaction(DeleteNote(
                noteId = note.id.noteId,
                i = account.getI(miCore.getEncryption())
            )).execute()
        }

        return miCore.getMisskeyAPI(account).createReaction(
            jp.panta.misskeyandroidclient.api.notes.CreateReaction(
                i = account.getI(miCore.getEncryption()),
                noteId = note.id.noteId,
                reaction = createReaction.reaction
            )
        ).execute().isSuccessful
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun unreaction(noteId: Note.Id): Boolean {
        val note = find(noteId)
        val account = miCore.getAccountRepository().get(noteId.accountId)
        return miCore.getMisskeyAPI(account).deleteReaction(DeleteNote(
            noteId = note.id.noteId,
            i = account.getI(miCore.getEncryption())
        )).execute().isSuccessful
    }
}