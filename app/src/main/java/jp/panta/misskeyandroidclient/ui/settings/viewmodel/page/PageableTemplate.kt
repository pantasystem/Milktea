package jp.panta.misskeyandroidclient.ui.settings.viewmodel.page

import net.pantasystem.milktea.data.model.account.page.Page
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.account.page.Pageable
import net.pantasystem.milktea.data.api.misskey.list.UserListDTO
import net.pantasystem.milktea.data.api.misskey.users.UserDTO
import net.pantasystem.milktea.data.model.antenna.Antenna


fun Account.newPage(pageable: Pageable, name: String): Page {
    return Page(this.accountId, name, 0, pageable)
}