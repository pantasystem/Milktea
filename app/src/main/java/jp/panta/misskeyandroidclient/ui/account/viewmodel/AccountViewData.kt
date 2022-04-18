package jp.panta.misskeyandroidclient.ui.account.viewmodel

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.users.viewmodel.UserViewData
import kotlinx.coroutines.*

@FlowPreview
@ExperimentalCoroutinesApi
class AccountViewData(
    val account: net.pantasystem.milktea.model.account.Account,
    miCore: MiCore,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserViewData(
    net.pantasystem.milktea.model.user.User.Id(account.accountId, account.remoteId),
    miCore,
    coroutineScope,
    coroutineDispatcher
)