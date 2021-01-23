package jp.panta.misskeyandroidclient.model.notes.reaction.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reaction_history")
data class ReactionHistory(
    val reaction: String,
    @ColumnInfo(name = "instance_domain")
    val instanceDomain: String
){
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}