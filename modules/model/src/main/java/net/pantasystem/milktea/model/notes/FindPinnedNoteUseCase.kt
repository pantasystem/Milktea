package net.pantasystem.milktea.model.notes

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindPinnedNoteUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val noteRepository: NoteRepository,
): UseCase {

    suspend operator fun invoke(userId: User.Id): Result<List<Note>> = runCancellableCatching{
        val detailedUser = userRepository.find(userId, true) as User.Detail
         noteRepository.findIn(detailedUser.info.pinnedNoteIds ?: emptyList())
    }
}