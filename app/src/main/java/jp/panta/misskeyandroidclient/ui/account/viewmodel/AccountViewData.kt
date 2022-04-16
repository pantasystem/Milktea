package jp.panta.misskeyandroidclient.ui.account.viewmodel

import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.users.viewmodel.UserViewData
import kotlinx.coroutines.*

@FlowPreview
@ExperimentalCoroutinesApi
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