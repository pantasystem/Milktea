package jp.panta.misskeyandroidclient.model.notes

import io.reactivex.Observable
import jp.panta.misskeyandroidclient.model.account.Account
import java.util.*

/**
 * Noteを管理するRepository
 * getしかない理由として
 * APIとLocalを抽象化する目的がある。
 * これはあくまでもViewModelなどに対してのインターフェースであって、実装クラスのためのものではないため
 * 削除や追加などのアクションはない。
 * add やremoveはAPIやローカルストレージが知ることである。
 */
interface NoteRepository {

    interface Factory{
        fun create(account: Account) : NoteRepository
    }

    suspend fun get(noteId: String) : Note?


}