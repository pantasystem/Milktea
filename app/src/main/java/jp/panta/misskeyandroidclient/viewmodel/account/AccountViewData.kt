package jp.panta.misskeyandroidclient.viewmodel.account

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData

data class AccountViewData(
    val account: Account
) : UserViewData(account.remoteId)