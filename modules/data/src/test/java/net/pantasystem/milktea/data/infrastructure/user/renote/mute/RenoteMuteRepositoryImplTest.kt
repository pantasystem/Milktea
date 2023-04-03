package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMuteDTO
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class RenoteMuteRepositoryImplTest {

    @Test
    fun renoteMuteDTO_toModel() {
        val now = Clock.System.now()
        val result = RenoteMuteDTO(
            id = "mute-id",
            muteeId = "user-id",
            mutee = UserDTO("user-id", ""),
            createdAt = now
        ).toModel(0L)

        val expect = RenoteMute(
            User.Id(0L, "user-id"),
            createdAt = now,
            postedAt = now,
        )
        Assertions.assertEquals(expect, result)
    }
}