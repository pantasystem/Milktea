package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMuteDTO
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class UnPushedRenoteMutesDiffFilterTest {

    @Test
    fun whenAlreadyExistsRemotely() {
        val target = UnPushedRenoteMutesDiffFilter()
        val createdAt = Clock.System.now()
        val result = target(
            mutes = listOf(
                RenoteMuteDTO(
                    id = "",
                    createdAt = createdAt,
                    mutee = UserDTO("user1", ""),
                    muteeId = "user1"
                ),
                RenoteMuteDTO(
                    id = "",
                    createdAt = createdAt,
                    mutee = UserDTO("user2", ""),
                    muteeId = "user2"
                ),
                RenoteMuteDTO(
                    id = "",
                    createdAt = createdAt,
                    mutee = UserDTO("user3", ""),
                    muteeId = "user3"
                ),
            ),
            locals = listOf(
                RenoteMute(
                    User.Id(0L, "user1"),
                    createdAt = createdAt,
                    postedAt = null,
                ),
                RenoteMute(
                    User.Id(0L, "user2"),
                    createdAt = createdAt,
                    postedAt = null,
                ),
                RenoteMute(
                    User.Id(0L, "user3"),
                    createdAt = createdAt,
                    postedAt = null,
                ),
            )
        )
        Assertions.assertEquals(listOf<RenoteMute>(), result)
    }

    @Test
    fun whenMoreLocalThanRemote() {
        val target = UnPushedRenoteMutesDiffFilter()
        val createdAt = Clock.System.now()
        val result = target(
            mutes = listOf(
                RenoteMuteDTO(
                    id = "",
                    createdAt = createdAt,
                    mutee = UserDTO("user1", ""),
                    muteeId = "user1"
                ),
                RenoteMuteDTO(
                    id = "",
                    createdAt = createdAt,
                    mutee = UserDTO("user2", ""),
                    muteeId = "user2"
                ),
            ),
            locals = listOf(
                RenoteMute(
                    User.Id(0L, "user1"),
                    createdAt = createdAt,
                    postedAt = null,
                ),
                RenoteMute(
                    User.Id(0L, "user2"),
                    createdAt = createdAt,
                    postedAt = null,
                ),
                RenoteMute(
                    User.Id(0L, "user3"),
                    createdAt = createdAt,
                    postedAt = null,
                ),
            )
        )
        Assertions.assertEquals(
            listOf(
                RenoteMute(
                    User.Id(0L, "user3"),
                    createdAt = createdAt,
                    postedAt = null,
                ),
            ), result
        )
    }

    @Test
    fun whenMoreRemoteThanLocal() {
        val target = UnPushedRenoteMutesDiffFilter()
        val createdAt = Clock.System.now()
        val result = target(
            mutes = listOf(
                RenoteMuteDTO(
                    id = "",
                    createdAt = createdAt,
                    mutee = UserDTO("user1", ""),
                    muteeId = "user1"
                ),
                RenoteMuteDTO(
                    id = "",
                    createdAt = createdAt,
                    mutee = UserDTO("user2", ""),
                    muteeId = "user2"
                ),
                RenoteMuteDTO(
                    id = "",
                    createdAt = createdAt,
                    mutee = UserDTO("user3", ""),
                    muteeId = "user3"
                ),
            ),
            locals = listOf(
                RenoteMute(
                    User.Id(0L, "user1"),
                    createdAt = createdAt,
                    postedAt = null,
                ),
                RenoteMute(
                    User.Id(0L, "user2"),
                    createdAt = createdAt,
                    postedAt = null,
                ),
            )
        )
        Assertions.assertEquals(listOf<RenoteMute>(), result)
    }

    @Test
    fun hasPushed() {
        val target = UnPushedRenoteMutesDiffFilter()
        val createdAt = Clock.System.now()
        val result = target(
            mutes = listOf(
                RenoteMuteDTO(
                    id = "",
                    createdAt = createdAt,
                    mutee = UserDTO("user1", ""),
                    muteeId = "user1"
                ),
                RenoteMuteDTO(
                    id = "",
                    createdAt = createdAt,
                    mutee = UserDTO("user2", ""),
                    muteeId = "user2"
                ),
            ),
            locals = listOf(
                RenoteMute(
                    User.Id(0L, "user1"),
                    createdAt = createdAt,
                    postedAt = null,
                ),
                RenoteMute(
                    User.Id(0L, "user2"),
                    createdAt = createdAt,
                    postedAt = null,
                ),
                RenoteMute(
                    User.Id(0L, "user3"),
                    createdAt = createdAt,
                    postedAt = createdAt,
                ),
            )
        )
        Assertions.assertEquals(
            listOf<RenoteMute>(),
            result
        )
    }
}