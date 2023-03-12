package net.pantasystem.milktea.data.infrastructure.user.renote.mute.db

import androidx.room.Entity
import androidx.room.Index
import kotlinx.datetime.Instant

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
    val accountId: Long,
    val userId: String,
    val createdAt: Instant,
    val postedAt: Instant?
)