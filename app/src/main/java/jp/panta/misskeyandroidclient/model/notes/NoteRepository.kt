package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.account.Account

/**
 * キャッシュやデータベースの実装の差をなくすためのRepository
 * 要するに抽象化したいだけで意味はない
 * ※またAPIを抽象化するためのものではない
 */
interface NoteRepository {

    interface Factory{
        fun create(account: Account) : NoteRepository
    }

    suspend fun get(noteId: String) : Note?

    suspend fun delete(noteId: String) : Boolean

    suspend fun add(note: Note) : Boolean

}