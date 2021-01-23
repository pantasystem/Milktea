package jp.panta.misskeyandroidclient.model.notes

import io.reactivex.Observable
import jp.panta.misskeyandroidclient.api.notes.Note
import jp.panta.misskeyandroidclient.model.account.Account
import java.util.*

interface NoteRepository {

    interface Factory{
        fun create(account: Account) : NoteRepository
    }

    data class Event(val note: Note, val type: Type, val createdAt: Date = Date()){

        enum class Type{
            CREATED, UPDATED, DELETED
        }
    }

    val account: Account

    suspend fun add(note: Note) : Note

    suspend fun get(noteId: String) : Note?

    suspend fun remove(note: Note)

    fun getEventStream(date: Date): Observable<Event>

}