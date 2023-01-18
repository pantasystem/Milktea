package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.notes.CreateReactionDTO
import net.pantasystem.milktea.api.misskey.notes.DeleteNote
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteCaptureAPIWithAccountProvider
import net.pantasystem.milktea.data.infrastructure.notes.onIReacted
import net.pantasystem.milktea.data.infrastructure.notes.onIUnReacted
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.reaction.CreateReaction
import net.pantasystem.milktea.model.notes.reaction.ReactionRepository
import javax.inject.Inject

class ReactionRepositoryImpl @Inject constructor(
    private val getAccount: GetAccount,
    private val noteCaptureAPIProvider: NoteCaptureAPIWithAccountProvider,
    private val noteRepository: NoteRepository,
    private val noteDataSource: NoteDataSource,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
): ReactionRepository {

    override suspend fun create(createReaction: CreateReaction): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(createReaction.noteId.accountId)
            val note = noteRepository.find(createReaction.noteId).getOrThrow()

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

    override suspend fun delete(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val note = noteRepository.find(noteId).getOrThrow()
            val account = getAccount.get(noteId.accountId)
            postUnReaction(noteId)
                    && (noteCaptureAPIProvider.get(account).isCaptured(noteId.noteId)
                    || (note.myReaction != null
                    && noteDataSource.add(note.onIUnReacted()).getOrThrow() != AddResult.Canceled))
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
        val note = noteRepository.find(noteId).getOrThrow()
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
}