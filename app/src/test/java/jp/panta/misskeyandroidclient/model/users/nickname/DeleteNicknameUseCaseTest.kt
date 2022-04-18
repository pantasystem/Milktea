package jp.panta.misskeyandroidclient.model.users.nickname

import jp.panta.misskeyandroidclient.logger.TestLogger

import jp.panta.misskeyandroidclient.model.account.TestAccountRepository

import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.data.infrastructure.user.impl.InMemoryUserDataSource
import net.pantasystem.milktea.data.infrastructure.user.impl.UserNicknameRepositoryOnMemoryImpl
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.nickname.DeleteNicknameUseCase
import net.pantasystem.milktea.model.user.nickname.UserNickname
import net.pantasystem.milktea.model.user.nickname.UserNicknameRepository
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test

class DeleteNicknameUseCaseTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var nicknameRepository: UserNicknameRepository
    private lateinit var userDataSource: UserDataSource
    private lateinit var user: User.Simple
    private lateinit var account: Account
    private lateinit var deleteNicknameUseCase: DeleteNicknameUseCase
    private lateinit var nicknameId: UserNickname.Id
    @Before
    fun setUp() {
        val accountRepository = TestAccountRepository()
        this.accountRepository = accountRepository
        account = accountRepository.accounts.values.first()
        val nicknameRepository = UserNicknameRepositoryOnMemoryImpl()
        this.nicknameRepository = nicknameRepository
        userDataSource = InMemoryUserDataSource(
            accountRepository = accountRepository,
            userNicknameRepository = nicknameRepository,
            loggerFactory = TestLogger.Factory()
        )

        user = User.Simple(
            User.Id(1, "remoteId"),
            "name",
            "name1",
            "",
            emptyList(),
            null,
            null,
            host = null,
            nickname = null
        )
        deleteNicknameUseCase = DeleteNicknameUseCase(
            accountRepository = accountRepository,
            userDataSource = userDataSource,
            userNicknameRepository = nicknameRepository
        )

        nicknameId = UserNickname.Id(userName = user.userName, host = account.getHost())
    }

    @Test
    fun deleteNickname() = runBlocking {
        val nickname = UserNickname(nicknameId, "changed name")
        nicknameRepository.save(nickname)
        userDataSource.add(user)
        assertEquals("changed name", userDataSource.get(user.id).getDisplayName())
        deleteNicknameUseCase.invoke(user)
        assertEquals("name1", userDataSource.get(user.id).getDisplayName())
    }
}