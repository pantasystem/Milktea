package net.pantasystem.milktea.model.notes.reaction.history

import androidx.room.ColumnInfo

data class ReactionHistoryCount(
    val reaction: String,
    val count: Int
)

data class ReactionHistoryCountRecord (
    val reaction: String,
    @ColumnInfo(name = "reaction_count")
    val count: Int
) {
    fun toReactionHistoryCount(): ReactionHistoryCount {
        return ReactionHistoryCount(reaction, count)
    }
}