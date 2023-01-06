package net.pantasystem.milktea.data.infrastructure.search

import androidx.room.*
import net.pantasystem.milktea.data.infrastructure.account.db.AccountRecord
import net.pantasystem.milktea.model.search.SearchHistory

@Entity(
    tableName = "search_histories",
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
) {

    @Ignore
    fun toModel(): SearchHistory {
        return SearchHistory(
            accountId = accountId,
            id = id,
            keyword = keyword
        )
    }
}

fun SearchHistory.toRecord(): SearchHistoryRecord {
    return SearchHistoryRecord(
        accountId = accountId,
        id = id,
        keyword = keyword
    )
}