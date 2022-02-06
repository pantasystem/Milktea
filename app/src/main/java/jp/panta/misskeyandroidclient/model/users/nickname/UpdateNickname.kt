package jp.panta.misskeyandroidclient.model.users.nickname

import jp.panta.misskeyandroidclient.model.UseCase
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserDataSource

class UpdateNicknameUseCase(
    val accountRepository: AccountRepository,
    val userDataSource: UserDataSource,
    val userNicknameRepository: UserNicknameRepository,
    val user: User,
    val nickname: String
) : UseCase<User> {

    override suspend fun execute(): User {
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
        val nickname = userNicknameRepository.findOne(id)
        userDataSource.add(
            when(user) {
                is User.Detail -> {
                    user.copy(nickname = nickname)
                }
                is User.Simple -> {
                    user.copy(nickname = nickname)
                }
            }
        )
        return user
    }
}