package jp.panta.misskeyandroidclient.model.users.nickname

import jp.panta.misskeyandroidclient.logger.TestLogger
import jp.panta.misskeyandroidclient.model.account.TestAccountRepository
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.data.infrastructure.user.impl.InMemoryUserDataSource
import net.pantasystem.milktea.data.infrastructure.user.impl.UserNicknameRepositoryOnMemoryImpl
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.nickname.UpdateNicknameUseCase
import net.pantasystem.milktea.model.user.nickname.UserNickname
import org.junit.Assert.*

import org.junit.Test

class UpdateNicknameUseCaseTest {



    @Test
    fun testUpdate() = runBlocking {
        val accountRepository = TestAccountRepository()
        val account = accountRepository.accounts.values.first()
        val nicknameRepository = UserNicknameRepositoryOnMemoryImpl()
        val userDataSource = InMemoryUserDataSource(
            accountRepository = accountRepository,
            userNicknameRepository = nicknameRepository,
            loggerFactory = TestLogger.Factory()
        )
        val updateNicknameUseCase = UpdateNicknameUseCase(
            accountRepository,
            userDataSource,
            nicknameRepository
        )
        val targetId = UserNickname.Id("name", account.getHost())

        val user = User.Simple(
            User.Id(1, "remoteId"),
            "name",
            "",
            "",
            emptyList(),
            null,
            null,
            host = null,
            nickname = null
        )
        userDataSource.add(user)

        nicknameRepository.save(
            UserNickname(targetId, "nickname")
        )
        val result = updateNicknameUseCase.invoke(user, "updated nickname")
        assertEquals("updated nickname", nicknameRepository.findOne(targetId).name)
        assertEquals("updated nickname", result.displayName)
        assertEquals("updated nickname", userDataSource.get(user.id).displayName)
    }
}