package net.pantasystem.milktea.data.model.account.db

import androidx.room.*
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Page

@DatabaseView
class AccountRelation{
    @Embedded lateinit var account: Account

    @Relation(parentColumn = "accountId", entityColumn = "accountId", entity = Page::class)
    lateinit var pages: List<Page>

    @Ignore
    fun toAccount(): Account {
        return account.copy(pages = pages.sortedBy {
            it.weight
        })
    }
}