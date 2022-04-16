package jp.panta.misskeyandroidclient.viewmodel.account

import jp.panta.misskeyandroidclient.viewmodel.MiCore
import net.pantasystem.milktea.data.model.account.CurrentAccountWatcher

fun MiCore.watchAccount(currentAccountId: Long? = null) : CurrentAccountWatcher {
    return CurrentAccountWatcher(currentAccountId, this.getAccountRepository())
}