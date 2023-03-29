package net.pantasystem.milktea.model.notes

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User

data class NoteDataSourceState(
    val map: Map<Note.Id, Note>
) {
    fun findIn(ids: List<Note.Id>) : List<Note>{
        return ids.mapNotNull {
            map[it]
        }
    }

    fun getOrNull(id: Note.Id) : Note? {
        return map[id]
    }
}

/**
 * キャッシュやデータベースの実装の差をなくすためのRepository
 * 要するに抽象化したいだけで意味はない
 * ※またAPIを抽象化するためのものではない
 */
interface NoteDataSource {

    interface Factory{
        fun create(account: Account) : NoteDataSource
    }

    fun interface Listener {
        fun on(e: Event)
    }



    sealed class Event{
        abstract val noteId: Note.Id
        data class Deleted(override val noteId: Note.Id) : Event()
        data class Updated(override val noteId: Note.Id, val note: Note): Event()
        data class Created(override val noteId: Note.Id, val note: Note): Event()
    }

    fun addEventListener(listener: Listener)

    suspend fun getIn(noteIds: List<Note.Id>) : Result<List<Note>>

    @Throws(NoteNotFoundException::class)
    suspend fun get(noteId: Note.Id) : Result<Note>

    suspend fun findByReplyId(id: Note.Id): Result<List<Note>>

    suspend fun exists(noteId: Note.Id) : Boolean

    /**
     * @param noteId 削除対象のNoteのId
     * キャッシュ上のノートを削除する。
     * これを実行すると削除フラグが立ち、
     * 次からgetなどの関数にアクセスすると、NoteDeletedExceptionの例外が投げられる
     */
    suspend fun delete(noteId: Note.Id) : Result<Boolean>

    /**
     * @param noteId 削除対象のNoteのId
     * キャッシュ上のノートを削除する。
     * これを実行するとキャッシュ削除フラグが立ち、
     * 次からgetなどの関数にアクセスすると、NoteDeletedExceptionの例外が投げられる
     */
    suspend fun remove(noteId: Note.Id) : Result<Boolean>

    suspend fun add(note: Note) : Result<AddResult>

    suspend fun addAll(notes: List<Note>) : Result<List<AddResult>>

    /**
     * 投稿者のuserIdに基づいて削除をします
     * @param userId 対称のUser#id
     * @return 削除されたNote数
     */
    suspend fun deleteByUserId(userId: User.Id) : Result<Int>

    fun observeIn(noteIds: List<Note.Id>): Flow<List<Note>>

    fun observeOne(noteId: Note.Id): Flow<Note?>

}