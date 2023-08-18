package net.pantasystem.milktea.data.infrastructure.note.reaction.impl.history

import androidx.room.ColumnInfo

data class FrequentlyReactionAndUnFollowedUserRecord(
    @ColumnInfo(name = "targetUserId") val targetUserId: String,
    @ColumnInfo(name = "accountId") val accountId: Long,
    @ColumnInfo(name = "reactionCount") val reactionCount: Int,
)