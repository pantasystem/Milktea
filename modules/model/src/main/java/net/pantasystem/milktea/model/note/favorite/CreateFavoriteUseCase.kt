package net.pantasystem.milktea.model.note.favorite

import net.pantasystem.milktea.model.note.Note
import javax.inject.Inject

class CreateFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(noteId: Note.Id): Result<Unit> {
        return favoriteRepository.create(noteId)
    }
}