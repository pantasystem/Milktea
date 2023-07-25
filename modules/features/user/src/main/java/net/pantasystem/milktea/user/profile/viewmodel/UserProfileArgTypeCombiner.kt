package net.pantasystem.milktea.user.profile.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User

class UserProfileArgTypeCombiner(
    private val scope: CoroutineScope,
) {
    fun create(
        userIdFlow: StateFlow<String?>,
        fqdnUserNameFlow: StateFlow<String?>,
        currentAccountFlow: StateFlow<Account?>
    ): StateFlow<UserProfileArgType> {
        return combine(
            userIdFlow,
            fqdnUserNameFlow,
            currentAccountFlow,
        ) { userId, fqdnUserName, currentAccount ->
            when {
                userId != null && currentAccount != null -> {
                    UserProfileArgType.UserId(User.Id(currentAccount.accountId, userId))
                }

                fqdnUserName != null && currentAccount != null -> {
                    UserProfileArgType.FqdnUserName(fqdnUserName, currentAccount)
                }

                else -> {
                    UserProfileArgType.None
                }
            }
        }.stateIn(
            scope,
            SharingStarted.WhileSubscribed(5_000),
            UserProfileArgType.None
        )
    }
}