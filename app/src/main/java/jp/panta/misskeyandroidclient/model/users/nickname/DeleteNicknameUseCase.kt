package jp.panta.misskeyandroidclient.model.users.nickname

import jp.panta.misskeyandroidclient.model.UseCase
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserDataSource

class DeleteNicknameUseCase(
    val userNicknameRepository: UserNicknameRepository,
    val accountRepository: AccountRepository,
    val userDataSource: UserDataSource,
    val user: User
) : UseCase<Unit>{

    override suspend fun execute() {
        val account = accountRepository.get(user.id.accountId)
        userNicknameRepository.delete(
            UserNickname.Id(
                userName = user.userName,
                host = user.host ?: account.getHost()
            )
        )
        userDataSource.add(when(user) {
            is User.Detail -> user.copy(nickname = null)
            is User.Simple -> user.copy(nickname = null)
        })
    }
}