package jp.panta.misskeyandroidclient.model.account.db

import androidx.room.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Page

@DatabaseView
class AccountRelation{
    @Embedded lateinit var account: Account

    @Relation(parentColumn = "accountId", entityColumn = "accountId", entity = Page::class)
    lateinit var pages: List<Page>

    @Ignore
    fun toAccount(): Account{
        return account.copy(pages = pages.sortedBy {
            it.weight
        })
    }
}