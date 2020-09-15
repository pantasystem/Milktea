package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.account.Account

interface NoteRepository {

    interface Factory{
        fun create(account: Account) : NoteRepository
    }

    val account: Account

    suspend fun add(note: Note) : Note

    suspend fun get(noteId: String) : Note?

    suspend fun remove(note: Note)


}