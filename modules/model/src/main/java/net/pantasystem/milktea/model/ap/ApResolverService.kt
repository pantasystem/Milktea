package net.pantasystem.milktea.model.ap

import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class ApResolverService @Inject constructor(
    private val apResolverRepository: ApResolverRepository,
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
) {

    suspend fun resolve(noteId: Note.Id, resolveToAccountId: Long): Result<Note> {
        return noteRepository.find(noteId).mapCancellableCatching { note ->
            val host = userRepository.find(note.userId).host
            val noteUri = note.uri ?: "https://$host/notes/${note.id.noteId}"
            apResolverRepository.resolve(resolveToAccountId, noteUri).mapCancellableCatching {
                when (it) {
                    is ApResolver.TypeNote -> it.note
                    is ApResolver.TypeUser -> throw IllegalStateException("Cannot resolve user")
                }
            }.getOrThrow()
        }
    }


}