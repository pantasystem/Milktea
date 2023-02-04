package net.pantasystem.milktea.model.notes.favorite

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToggleFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val noteRepository: NoteRepository,
) : UseCase {

    suspend operator fun invoke(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        val isFavorite = when(val type = noteRepository.find(noteId).getOrThrow().type) {
            is Note.Type.Mastodon -> {
                type.favorited == true
            }
            is Note.Type.Misskey -> {
                val state = noteRepository.findNoteState(noteId).getOrThrow()
                state.isFavorited
            }
        }
        if (isFavorite) {
            favoriteRepository.delete(noteId)
        } else {
            favoriteRepository.create(noteId)
        }
    }
}