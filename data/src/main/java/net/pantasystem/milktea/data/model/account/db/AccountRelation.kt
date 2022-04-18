package net.pantasystem.milktea.data.model.account.db

import androidx.room.*
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Page

@DatabaseView
class AccountRelation{
    @Embedded lateinit var account: net.pantasystem.milktea.model.account.Account

    @Relation(parentColumn = "accountId", entityColumn = "accountId", entity = net.pantasystem.milktea.model.account.page.Page::class)
    lateinit var pages: List<net.pantasystem.milktea.model.account.page.Page>

    @Ignore
    fun toAccount(): net.pantasystem.milktea.model.account.Account {
        return account.copy(pages = pages.sortedBy {
            it.weight
        })
    }
}