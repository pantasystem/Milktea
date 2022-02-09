package jp.panta.misskeyandroidclient.model.notes.reaction.history

import androidx.room.ColumnInfo

data class ReactionHistoryCount(
    val reaction: String,
    @ColumnInfo(name = "reaction_count")
    val count: Int
)