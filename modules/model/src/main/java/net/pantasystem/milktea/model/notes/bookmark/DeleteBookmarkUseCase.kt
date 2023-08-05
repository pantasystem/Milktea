package net.pantasystem.milktea.model.notes.bookmark

import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.notes.Note
import javax.inject.Inject

class DeleteBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : UseCase {

    suspend operator fun invoke(noteId: Note.Id): Result<Unit> {
        return bookmarkRepository.delete(noteId)
    }
}