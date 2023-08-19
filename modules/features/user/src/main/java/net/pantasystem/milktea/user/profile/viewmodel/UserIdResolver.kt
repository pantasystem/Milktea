package net.pantasystem.milktea.user.profile.viewmodel

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.Acct
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class UserIdResolver @Inject constructor(
    val accountRepository: AccountRepository,
    val userRepository: UserRepository,
) {

    suspend operator fun invoke(
        userId: String?,
        acct: String?,
        specifiedAccountId: Long?,
    ): Result<User.Id> = runCancellableCatching {
        val currentAccount = specifiedAccountId?.let {
            accountRepository.get(it).getOrThrow()
        } ?: accountRepository.getCurrentAccount().getOrThrow()
        val argType = when {
            userId != null -> {
                UserProfileArgType.UserId(User.Id(currentAccount.accountId, userId
                ))
            }

            acct != null -> {
                UserProfileArgType.FqdnUserName(acct, currentAccount)
            }

            else -> {
                UserProfileArgType.None
            }
        }

        when (argType) {
            is UserProfileArgType.FqdnUserName -> {
                val (userName, host) = Acct(argType.fqdnUserName).let {
                    it.userName to it.host
                }
                userRepository.findByUserName(currentAccount.accountId, userName, host).id
            }

            is UserProfileArgType.UserId -> {
                argType.userId
            }

            UserProfileArgType.None -> throw IllegalStateException()
        }

    }
}