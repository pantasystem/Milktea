package jp.panta.misskeyandroidclient.model.users.nickname

import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.data.infrastructure.MemoryCacheCleaner
import net.pantasystem.milktea.data.infrastructure.user.InMemoryUserDataSource
import net.pantasystem.milktea.data.infrastructure.user.UserNicknameRepositoryOnMemoryImpl
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.nickname.DeleteNicknameUseCase
import net.pantasystem.milktea.model.user.nickname.UserNickname
import net.pantasystem.milktea.model.user.nickname.UserNicknameRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteNicknameUseCaseTest {

    private lateinit var nicknameRepository: UserNicknameRepository
    private lateinit var userDataSource: UserDataSource
    private lateinit var user: User.Simple
    private lateinit var deleteNicknameUseCase: DeleteNicknameUseCase
    private lateinit var nicknameId: UserNickname.Id
    @BeforeEach
    fun setUp() {
        val nicknameRepository = UserNicknameRepositoryOnMemoryImpl()
        this.nicknameRepository = nicknameRepository
        userDataSource = InMemoryUserDataSource(MemoryCacheCleaner())

        user = User.Simple(
            User.Id(1, "remoteId"),
            "name",
            "name1",
            "",
            emptyList(),
            null,
            null,
            host = "misskey.io",
            nickname = null,
            isSameHost = true,
            instance = null,
            avatarBlurhash = null,
            badgeRoles = emptyList(),
        )
        deleteNicknameUseCase = DeleteNicknameUseCase(
            userDataSource = userDataSource,
            userNicknameRepository = nicknameRepository
        )

        nicknameId = UserNickname.Id(userName = user.userName, host = "misskey.io")
    }

    @Test
    fun deleteNickname() {
        runBlocking {
            val nickname = UserNickname(nicknameId, "changed name")
            userDataSource.add(user.copy(nickname = nickname))
            nicknameRepository.save(nickname)
            assertEquals("changed name", userDataSource.get(user.id).getOrThrow().displayName)
            deleteNicknameUseCase.invoke(user)
            assertEquals("name1", userDataSource.get(user.id).getOrThrow().displayName)
        }
    }
}