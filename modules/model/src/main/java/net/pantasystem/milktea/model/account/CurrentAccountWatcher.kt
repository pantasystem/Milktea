package net.pantasystem.milktea.model.account

import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * アカウントを指定するとそのアカウントの状態を監視＆イベントを伝えます。
 * アカウントを指定しなかった場合現在のアカウントを返すようになります。
 * @param currentAccountId 監視するアカウントを固定する場合はここに対象のアカウントのIdを指定します。
 */
class CurrentAccountWatcher(
    var currentAccountId: Long?,
    val accountRepository: AccountRepository
) {
    @ExperimentalCoroutinesApi
    val account
        get() = currentAccountId?.let {
            accountRepository.watchAccount(it)
        }?: accountRepository.watchCurrentAccount()

    suspend fun getAccount() : Account {
        return currentAccountId?.let {
            accountRepository.get(it).getOrThrow()
        }?: accountRepository.getCurrentAccount().getOrThrow()
    }
}

