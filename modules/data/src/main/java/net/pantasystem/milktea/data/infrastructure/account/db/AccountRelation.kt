package net.pantasystem.milktea.data.infrastructure.account.db

import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import net.pantasystem.milktea.model.account.AccountRecord
import net.pantasystem.milktea.model.account.page.Page

@DatabaseView
class AccountRelation{
    @Embedded lateinit var account: AccountRecord

    @Relation(parentColumn = "accountId", entityColumn = "accountId", entity = Page::class)
    lateinit var pages: List<Page>

    @Ignore
    fun toAccount(): AccountRecord {
        return account.copy(pages = pages.sortedBy {
            it.weight
        })
    }
}