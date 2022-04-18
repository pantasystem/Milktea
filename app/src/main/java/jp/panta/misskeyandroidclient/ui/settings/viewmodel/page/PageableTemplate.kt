package jp.panta.misskeyandroidclient.ui.settings.viewmodel.page

import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable


fun Account.newPage(pageable: Pageable, name: String): Page {
    return Page(this.accountId, name, 0, pageable)
}