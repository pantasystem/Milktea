package net.pantasystem.milktea.app_store.account

import net.pantasystem.milktea.model.account.Account


data class AccountState(
    val currentAccountId: Long? = null,
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = true,
    val error: Throwable? = null
) {

    val currentAccount: Account?
        get() = accounts.firstOrNull { it.accountId == currentAccountId }
            ?: accounts.firstOrNull()

    val isUnauthorized: Boolean
        get() = accounts.isEmpty() && !isLoading

    fun hasAccount(account: Account): Boolean {
        return accounts.any { it.accountId == account.accountId }
    }

    fun get(accountId: Long): Account? {
        return accounts.firstOrNull { it.accountId == accountId }
    }

    fun add(account: Account): AccountState {
        return copy(
            accounts = if (hasAccount(account)) {
                accounts.map {
                    if (it.accountId == account.accountId) {
                        account
                    } else {
                        it
                    }
                }
            } else {
                accounts.toMutableList().also { list ->
                    list.add(account)
                }
            }.distinctBy {
                it.accountId
            },
            currentAccountId = if (accounts.isEmpty()) account.accountId else currentAccountId
        )
    }

    fun delete(accountId: Long): AccountState {
        val filtered = accounts.filterNot { it.accountId == accountId }
        return copy(
            currentAccountId = if (currentAccountId == accountId) {
                filtered.firstOrNull()?.accountId
            } else {
                currentAccountId
            },
            accounts = filtered
        )
    }

    fun setCurrentAccount(account: Account): AccountState {
        return add(account).copy(currentAccountId = account.accountId)
    }
}
