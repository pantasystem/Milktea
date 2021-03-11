package jp.panta.misskeyandroidclient.viewmodel.account

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AccountViewData(
    val account: Account,
    miCore: MiCore,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserViewData(
    User.Id(account.accountId, account.remoteId),
    miCore,
    coroutineScope,
    coroutineDispatcher
)