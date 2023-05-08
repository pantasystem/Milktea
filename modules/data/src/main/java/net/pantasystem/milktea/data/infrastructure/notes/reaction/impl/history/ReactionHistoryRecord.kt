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
    val instanceDomain: String,

    @ColumnInfo(name = "accountId")
    val accountId: Long? = null,

    @ColumnInfo(name = "target_post_id")
    val targetPostId: String? = null,

    @ColumnInfo(name = "target_user_id")
    val targetUserId: String? = null,
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    var id: Long? = null

    companion object {
        fun from(history: ReactionHistory): ReactionHistoryRecord {
            return ReactionHistoryRecord(
                reaction = history.reaction,
                instanceDomain = history.instanceDomain,
                targetUserId = history.targetUserId,
                targetPostId = history.targetPostId,
                accountId = history.accountId,
            ).apply {
                id = history.id
            }
        }
    }

    fun toHistory(): ReactionHistory {
        return ReactionHistory(
            reaction = reaction,
            instanceDomain = instanceDomain,
            accountId = accountId,
            targetPostId = targetPostId,
            targetUserId = targetUserId,
            id = id,
        )
    }
}