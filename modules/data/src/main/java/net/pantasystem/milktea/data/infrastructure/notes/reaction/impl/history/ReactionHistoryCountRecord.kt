package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.history

import androidx.room.ColumnInfo
import net.pantasystem.milktea.model.note.reaction.history.ReactionHistoryCount

data class ReactionHistoryCountRecord (
    val reaction: String,
    @ColumnInfo(name = "reaction_count")
    val count: Int
) {
    fun toReactionHistoryCount(): ReactionHistoryCount {
        return ReactionHistoryCount(reaction, count)
    }
}