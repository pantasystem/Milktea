package net.pantasystem.milktea.model.notes.favorite

import net.pantasystem.milktea.model.notes.Note
import javax.inject.Inject

class DeleteFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(noteId: Note.Id): Result<Unit> {
        return favoriteRepository.delete(noteId)
    }
}