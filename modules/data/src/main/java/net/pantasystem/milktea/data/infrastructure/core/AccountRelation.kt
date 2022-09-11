@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.data.infrastructure.core

import android.util.Log
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Relation
import net.pantasystem.milktea.data.infrastructure.Page

@Suppress("DEPRECATION")
@Deprecated("model.account.Accountへ移行")
@Entity
class AccountRelation{
    @Embedded
    lateinit var account: Account

    @Relation(parentColumn = "id", entityColumn = "accountId", entity = EncryptedConnectionInformation::class)
    lateinit var connectionInformationList: List<EncryptedConnectionInformation>

    /*@Relation(parentColumn = "id", entityColumn = "accountId", entity = NoteRequest.Setting::class)
    lateinit var pages: List<NoteRequest.Setting>*/

    @Relation(parentColumn = "id", entityColumn = "accountId", entity = Page::class)
    lateinit var pages: List<Page>

    @Ignore
    fun getCurrentConnectionInformation(): EncryptedConnectionInformation?{
        return try{
            connectionInformationList.maxByOrNull { e ->
                e.updatedAt.time
            }
        }catch(e: UninitializedPropertyAccessException){
            Log.d("AccountRelation", "error", e)
            null
        }
    }

    override fun toString(): String {
        return "AccountRelation(account=$account, connectionInformationList=$connectionInformationList, pages=$pages)"
    }
}