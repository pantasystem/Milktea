package jp.panta.misskeyandroidclient.model.users.nickname

import jp.panta.misskeyandroidclient.model.UseCase
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteNicknameUseCase @Inject constructor(
    val userNicknameRepository: UserNicknameRepository,
    val accountRepository: AccountRepository,
    val userDataSource: UserDataSource,
) : UseCase {

    suspend operator fun invoke(user: User) {
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