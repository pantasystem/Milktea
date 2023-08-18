package net.pantasystem.milktea.model.note.bookmark

import net.pantasystem.milktea.model.note.Note
import javax.inject.Inject

class CreateBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {

    suspend operator fun invoke(noteId: Note.Id): Result<Unit> {
        return bookmarkRepository.create(noteId)
    }
}