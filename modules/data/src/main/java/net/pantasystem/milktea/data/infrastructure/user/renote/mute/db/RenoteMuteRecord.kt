package net.pantasystem.milktea.data.infrastructure.user.renote.mute.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute

@Entity(
    tableName = "renote_mute_users",
    primaryKeys = [
        "userId", "accountId"
    ],
    indices = [
        Index("postedAt"),
        Index("accountId")
    ]
)
data class RenoteMuteRecord(
    @ColumnInfo(name = "accountId")
    val accountId: Long,

    @ColumnInfo(name = "userId")
    val userId: String,

    @ColumnInfo(name = "createdAt")
    val createdAt: Instant,

    @ColumnInfo(name = "postedAt")
    val postedAt: Instant?,
) {

    fun toModel(): RenoteMute {
        return RenoteMute(
            User.Id(accountId, userId),
            createdAt = createdAt,
            postedAt = postedAt,
        )
    }

    companion object {
        fun from(model: RenoteMute): RenoteMuteRecord {
            return RenoteMuteRecord(
                accountId = model.userId.accountId,
                userId = model.userId.id,
                createdAt = model.createdAt,
                postedAt = model.postedAt,
            )
        }
    }
}