package net.pantasystem.milktea.data.infrastructure.account.db

import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import net.pantasystem.milktea.data.infrastructure.account.page.db.PageRecord

@DatabaseView
class AccountRelation{
    @Embedded lateinit var account: AccountRecord

    @Relation(parentColumn = "accountId", entityColumn = "accountId", entity = PageRecord::class)
    lateinit var pages: List<PageRecord>

    @Ignore
    fun toAccount(): AccountRecord {
        return account.copy(pages = pages.sortedBy {
            it.weight
        }.map {
            it.toPage()
        })
    }
}