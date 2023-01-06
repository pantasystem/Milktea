package net.pantasystem.milktea.data.infrastructure.search

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import net.pantasystem.milktea.data.infrastructure.account.db.AccountRecord

@Entity(
    indices = [Index("keyword", "accountId", unique = true), Index("accountId")],
    foreignKeys = [
        ForeignKey(
            parentColumns = ["accountId"],
            entity = AccountRecord::class,
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            childColumns = ["accountId"]
        )
    ]
)
data class SearchHistoryRecord(
    val accountId: Long,
    val keyword: String,
    @PrimaryKey(autoGenerate = true) val id: Long
)