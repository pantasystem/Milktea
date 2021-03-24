package jp.panta.misskeyandroidclient.model.notes.reaction.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class ReactionHistoryCount(
    val reaction: String,
    @ColumnInfo(name = "reaction_count")
    val count: Int
)