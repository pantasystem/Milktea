package net.pantasystem.milktea.model.user.nickname

import net.pantasystem.milktea.model.UseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteNicknameUseCase @Inject constructor(
    val userNicknameRepository: UserNicknameRepository,
    val accountRepository: net.pantasystem.milktea.model.account.AccountRepository,
    val userDataSource: net.pantasystem.milktea.model.user.UserDataSource,
) : UseCase {

    suspend operator fun invoke(user: net.pantasystem.milktea.model.user.User) {
        val account = accountRepository.get(user.id.accountId)
        userNicknameRepository.delete(
            UserNickname.Id(
                userName = user.userName,
                host = user.host ?: account.getHost()
            )
        )
        userDataSource.add(when(user) {
            is net.pantasystem.milktea.model.user.User.Detail -> user.copy(nickname = null)
            is net.pantasystem.milktea.model.user.User.Simple -> user.copy(nickname = null)
        })
    }
}