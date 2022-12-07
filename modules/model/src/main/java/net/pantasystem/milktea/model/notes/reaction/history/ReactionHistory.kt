package net.pantasystem.milktea.model.notes.reaction.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class ReactionHistory(
    val reaction: String,
    val instanceDomain: String,
    var id: Long? = null
)

@Entity(tableName = "reaction_history")
data class ReactionHistoryRecord(
    val reaction: String,
    @ColumnInfo(name = "instance_domain")
    val instanceDomain: String
){
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    companion object {
        fun from(history: ReactionHistory): ReactionHistoryRecord {
            return ReactionHistoryRecord(
                reaction = history.reaction,
                instanceDomain = history.instanceDomain,
            ).apply {
                id = history.id
            }
        }
    }

    fun toHistory(): ReactionHistory {
        return ReactionHistory(reaction, instanceDomain, id)
    }
}