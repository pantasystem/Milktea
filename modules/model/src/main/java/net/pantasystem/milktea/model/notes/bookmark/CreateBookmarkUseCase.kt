package net.pantasystem.milktea.model.notes.bookmark

import net.pantasystem.milktea.model.notes.Note
import javax.inject.Inject

class CreateBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {

    suspend operator fun invoke(noteId: Note.Id): Result<Unit> {
        return bookmarkRepository.create(noteId)
    }
}