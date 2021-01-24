package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.account.Account
import kotlinx.coroutines.flow.Flow

/**
 * キャッシュやデータベースの実装の差をなくすためのRepository
 * 要するに抽象化したいだけで意味はない
 * ※またAPIを抽象化するためのものではない
 */
interface NoteRepository {

    interface Factory{
        fun create(account: Account) : NoteRepository
    }



    sealed class Event{
        data class Deleted(val noteId: String) : Event()
        data class Added(val note: Note): Event()
    }
    fun observer(): Flow<Event>

    suspend fun get(noteId: String) : Note?

    suspend fun remove(noteId: String) : Boolean

    suspend fun add(note: Note) : AddResult

    /**
     * 投稿者のuserIdに基づいて削除をします
     * @param userId 対称のUser#id
     * @return 削除されたNote数
     */
    suspend fun removeByUserId(userId: String) : Int

}