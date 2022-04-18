package jp.panta.misskeyandroidclient.viewmodel.account

import jp.panta.misskeyandroidclient.viewmodel.MiCore
import net.pantasystem.milktea.model.account.CurrentAccountWatcher

fun MiCore.watchAccount(currentAccountId: Long? = null) : net.pantasystem.milktea.model.account.CurrentAccountWatcher {
    return net.pantasystem.milktea.model.account.CurrentAccountWatcher(
        currentAccountId,
        this.getAccountRepository()
    )
}