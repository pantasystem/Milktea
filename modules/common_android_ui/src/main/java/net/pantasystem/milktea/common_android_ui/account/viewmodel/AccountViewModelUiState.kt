package net.pantasystem.milktea.common_android_ui.account.viewmodel

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.instance.InstanceInfoType
import net.pantasystem.milktea.model.user.User

data class AccountInfo(
    val account: Account,
    val user: User?,
    val instanceMeta: InstanceInfoType?,
    val isCurrentAccount: Boolean,
)

data class AccountViewModelUiState(
    val currentAccount: Account? = null,
    val accounts: List<AccountInfo> = emptyList(),
) {
    val currentAccountInfo: AccountInfo? by lazy {
        accounts.firstOrNull {
            it.account.accountId == currentAccount?.accountId
        }
    }
}

fun List<Account>.toAccountInfoList(
    currentAccount: Account?,
    instanceInfoList: List<InstanceInfoType?>,
    users: List<User>,
): List<AccountInfo> {
    val userMap = users.associateBy {
        it.id.accountId
    }
    val metaMap = instanceInfoList.filterNotNull().associateBy {
        it.uri
    }
    return map {
        AccountInfo(
            it,
            userMap[it.accountId],
            metaMap[it.normalizedInstanceUri],
            currentAccount?.accountId == it.accountId
        )
    }
}