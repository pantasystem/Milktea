package jp.panta.misskeyandroidclient.ui.settings.viewmodel.page

import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable


fun net.pantasystem.milktea.model.account.Account.newPage(pageable: net.pantasystem.milktea.model.account.page.Pageable, name: String): net.pantasystem.milktea.model.account.page.Page {
    return Page(this.accountId, name, 0, pageable)
}