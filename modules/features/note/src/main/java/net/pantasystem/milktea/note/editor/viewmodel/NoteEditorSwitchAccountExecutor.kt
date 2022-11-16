package net.pantasystem.milktea.note.editor.viewmodel

import net.pantasystem.milktea.app_store.notes.NoteEditingState
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.ap.ApResolver
import net.pantasystem.milktea.model.ap.ApResolverRepository
import net.pantasystem.milktea.model.notes.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteEditorSwitchAccountExecutor  @Inject constructor(
    private val resolverRepository: ApResolverRepository,
    private val noteRepository: NoteRepository,
){

    suspend operator fun invoke(noteEditingState: NoteEditingState, switchToAccount: Account): Result<NoteEditingState> = runCatching  {
        val fromAccount = noteEditingState.author
        if (fromAccount?.getHost() == switchToAccount.getHost()) {
            noteEditingState.setAccount(switchToAccount)
        }

        var convertTo = noteEditingState

        if (convertTo.replyId != null) {
            val reply = noteRepository.find(convertTo.replyId!!).getOrThrow()
            val url = "${fromAccount?.instanceDomain}/notes/${reply.id.noteId}"
            val toReplyNote = resolverRepository.resolve(switchToAccount.accountId, url).getOrThrow() as ApResolver.TypeNote
            convertTo = convertTo.copy(replyId = toReplyNote.note.id)
        }

        if (convertTo.renoteId != null) {
            val renote = noteRepository.find(convertTo.renoteId!!).getOrThrow()
            val url = "${fromAccount?.instanceDomain}/notes/${renote.id.noteId}"
            val toRenoteNote = resolverRepository.resolve(switchToAccount.accountId, url).getOrThrow() as ApResolver.TypeNote
            convertTo = convertTo.copy(renoteId = toRenoteNote.note.id)
        }

        convertTo.setAccount(switchToAccount)
    }
}