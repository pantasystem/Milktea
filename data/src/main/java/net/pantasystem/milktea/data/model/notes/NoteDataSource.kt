package net.pantasystem.milktea.data.model.notes

import net.pantasystem.milktea.data.model.AddResult
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.users.User
import kotlin.jvm.Throws

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


    @Throws(NoteNotFoundException::class)
    suspend fun get(noteId: Note.Id) : Note

    suspend fun remove(noteId: Note.Id) : Boolean

    suspend fun add(note: Note) : AddResult

    suspend fun addAll(notes: List<Note>) : List<AddResult>

    /**
     * 投稿者のuserIdに基づいて削除をします
     * @param userId 対称のUser#id
     * @return 削除されたNote数
     */
    suspend fun removeByUserId(userId: User.Id) : Int

}