package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistory

@Entity(tableName = "reaction_history")
data class ReactionHistoryRecord(
    @ColumnInfo("reaction")
    val reaction: String,

    @ColumnInfo(name = "instance_domain")
    val instanceDomain: String
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
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