package net.pantasystem.milktea.model.note.reaction

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.user.User

interface ReactionUserRepository {
    /**
     * 内部的に保持しているリアクションしたユーザの履歴の状態をリモートと同期する
     * @param reaction 同期する対象のリアクションでカスタム絵文字の場合は:emoji_type@.:あるいは:emoji_type@host:の形式で与えられる
     * @param noteId 同期する対象のNoteのId
     */
    suspend fun syncBy(noteId: Note.Id, reaction: String? = null): Result<Unit>

    /**
     * 内部的に保持しているリアクションしたユーザの履歴の状態をobserveする
     * @param reaction 同期する対象のリアクションでカスタム絵文字の場合は:emoji_type@.:あるいは:emoji_type@host:の形式で与えられる
     * @param noteId 同期する対象のNoteのId
     */
    fun observeBy(noteId: Note.Id, reaction: String? = null): Flow<List<User>>

    /**
     * 内部的に保持しているリアクションしたユーザの履歴の現在の状態を参照する
     * @param reaction 同期する対象のリアクションでカスタム絵文字の場合は:emoji_type@.:あるいは:emoji_type@host:の形式で与えられる
     * @param noteId 同期する対象のNoteのId
     */
    suspend fun findBy(noteId: Note.Id, reaction: String? = null): Result<List<User>>
}