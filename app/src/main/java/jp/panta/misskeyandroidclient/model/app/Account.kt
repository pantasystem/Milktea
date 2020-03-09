package jp.panta.misskeyandroidclient.model.app

import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.notes.NoteRequest

class Account(
    @PrimaryKey(autoGenerate = false)
    val id: String
){

    @Relation(parentColumn = "id", entityColumn = "accountId")
    lateinit var connectionInformationList: List<ConnectionInformation>

    @Relation(parentColumn = "id", entityColumn = "accountId")
    lateinit var pages: List<NoteRequest.Setting>
    @Ignore
    fun getI(encryption: Encryption): String?{
        return ""
    }
}