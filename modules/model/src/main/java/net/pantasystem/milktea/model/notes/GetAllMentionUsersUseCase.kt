package net.pantasystem.milktea.model.notes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class GetAllMentionUsersUseCase @Inject constructor(
    val noteRepository: NoteRepository,
    val userRepository: UserRepository,
    val getAccount: GetAccount,
) : UseCase {

    /**
     * 返信元を辿りながら、全てのユーザーを取得する
     * @param noteId 指定されたNoteのIdから親投稿のユーザーを取得していきながらメンション対象のユーザを取得します。
     */
    suspend operator fun invoke(
        noteId: Note.Id,
        users: List<User> = emptyList(),
        searchedIds: List<Note.Id> = emptyList()
    ): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            // NOTE:
            if (searchedIds.contains(noteId)) {
                return@withContext Result.success(users)
            }
            runCancellableCatching {
                val me = getAccount.get(noteId.accountId)
                val myId = User.Id(me.accountId, me.remoteId)
                val note = noteRepository.find(noteId).getOrThrow()
                val user = userRepository.find(note.userId)
                if (note.replyId == null) {
                    (users + user).distinctBy { it.id }.filterNot {
                        it.id == myId
                    }
                } else {
                    invoke(note.replyId, users + user, searchedIds + noteId)
                        .getOrElse {
                            users.distinctBy { it.id }.filterNot {
                                it.id == myId
                            }
                        }
                }
            }
        }

    }

}