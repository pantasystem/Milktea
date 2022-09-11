package net.pantasystem.milktea.model.notes.reaction.history

import androidx.room.ColumnInfo

data class ReactionHistoryCount(
    val reaction: String,
    @ColumnInfo(name = "reaction_count")
    val count: Int
)