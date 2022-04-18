package net.pantasystem.milktea.model.user.nickname


import net.pantasystem.milktea.model.UseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateNicknameUseCase @Inject constructor(
    val accountRepository: net.pantasystem.milktea.model.account.AccountRepository,
    val userDataSource: net.pantasystem.milktea.model.user.UserDataSource,
    val userNicknameRepository: UserNicknameRepository,
) : UseCase {

    suspend operator fun invoke(user: net.pantasystem.milktea.model.user.User, nickname: String): net.pantasystem.milktea.model.user.User {
        val account = accountRepository.get(user.id.accountId)
        val id = UserNickname.Id(
            userName = user.userName,
            host = user.host ?: account.getHost()
        )
        userNicknameRepository.save(
            UserNickname(
                id = id,
                name = nickname
            )
        )
        val existsUserName = userNicknameRepository.findOne(id)
        val updatedUser = when(user) {
            is net.pantasystem.milktea.model.user.User.Detail -> {
                user.copy(nickname = existsUserName)
            }
            is net.pantasystem.milktea.model.user.User.Simple -> {
                user.copy(nickname = existsUserName)
            }
        }
        userDataSource.add(
            updatedUser
        )
        return updatedUser
    }
}