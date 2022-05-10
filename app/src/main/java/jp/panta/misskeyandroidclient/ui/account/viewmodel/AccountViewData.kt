package jp.panta.misskeyandroidclient.ui.account.viewmodel

import jp.panta.misskeyandroidclient.ui.users.viewmodel.UserViewData
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User

@FlowPreview
@ExperimentalCoroutinesApi
class AccountViewData(
    val account: Account,
    miCore: MiCore,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserViewData(
    User.Id(account.accountId, account.remoteId),
    miCore.getUserDataSource(),
    miCore.getUserRepository(),
    miCore.loggerFactory.create("AccountViewData"),
    coroutineScope,
    coroutineDispatcher
)