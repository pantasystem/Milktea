package jp.panta.misskeyandroidclient.model.users.nickname

import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.data.infrastructure.MemoryCacheCleaner
import net.pantasystem.milktea.data.infrastructure.user.InMemoryUserDataSource
import net.pantasystem.milktea.data.infrastructure.user.UserNicknameRepositoryOnMemoryImpl
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.nickname.UpdateNicknameUseCase
import net.pantasystem.milktea.model.user.nickname.UserNickname
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class UpdateNicknameUseCaseTest {


    @Test
    fun testUpdate() {
        runBlocking {
            val nicknameRepository = UserNicknameRepositoryOnMemoryImpl()
            val userDataSource = InMemoryUserDataSource(MemoryCacheCleaner())
            val updateNicknameUseCase = UpdateNicknameUseCase(
                userDataSource,
                nicknameRepository
            )
            val targetId = UserNickname.Id("name", "misskey.io")

            val user = User.Simple(
                User.Id(1, "remoteId"),
                "name",
                "",
                "",
                emptyList(),
                null,
                null,
                host = "misskey.io",
                nickname = null,
                isSameHost = true,
                instance = null,
                avatarBlurhash = null,
            )
            userDataSource.add(user)

            nicknameRepository.save(
                UserNickname(targetId, "nickname")
            )
            val result = updateNicknameUseCase.invoke(user, "updated nickname")
            assertEquals("updated nickname", nicknameRepository.findOne(targetId).name)
            assertEquals("updated nickname", result.displayName)
            assertEquals("updated nickname", userDataSource.get(user.id).getOrThrow().displayName)
        }
    }
}