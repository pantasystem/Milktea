package jp.panta.misskeyandroidclient.model.users.nickname

import jp.panta.misskeyandroidclient.model.UseCase
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateNicknameUseCase @Inject constructor(
    val accountRepository: AccountRepository,
    val userDataSource: UserDataSource,
    val userNicknameRepository: UserNicknameRepository,
) : UseCase {

    suspend operator fun invoke(user: User, nickname: String): User {
        val account = accountRepository.get(user.id.accountId)
        val id = UserNickname.Id(
            userName = user.userName,
            host = user.host?: account.getHost()
        )
        userNicknameRepository.save(
            UserNickname(
                id = id,
                name = nickname
            )
        )
        val existsUserName = userNicknameRepository.findOne(id)
        userDataSource.add(
            when(user) {
                is User.Detail -> {
                    user.copy(nickname = existsUserName)
                }
                is User.Simple -> {
                    user.copy(nickname = existsUserName)
                }
            }
        )
        return user
    }
}