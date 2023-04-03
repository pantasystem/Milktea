package net.pantasystem.milktea.data.infrastructure.user.renote.mute.db

import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days

internal class RenoteMuteRecordTest {

    @Test
    fun toModel() {
        val createdAt = Clock.System.now()
        val postedAt = Clock.System.now() + 1.days
        val record = RenoteMuteRecord(
            accountId = 0, userId = "user-id-1", createdAt = createdAt, postedAt = postedAt
        )

        val expect = RenoteMute(
            User.Id(0L, "user-id-1"),
            createdAt = createdAt,
            postedAt = postedAt,
        )
        Assertions.assertEquals(expect, record.toModel())
    }

    @Test
    fun toModel_GiveNullPostedAt() {
        val createdAt = Clock.System.now()
        val record = RenoteMuteRecord(
            accountId = 0, userId = "user-id-1", createdAt = createdAt, postedAt = null
        )

        val expect = RenoteMute(
            User.Id(0L, "user-id-1"),
            createdAt = createdAt,
            postedAt = null
        )
        Assertions.assertEquals(expect, record.toModel())
    }

    @Test
    fun from() {
        val createdAt = Clock.System.now()
        val postedAt = Clock.System.now() + 1.days

        val model = RenoteMute(
            User.Id(0L, "user-id-1"),
            createdAt = createdAt,
            postedAt = postedAt
        )

        val expect = RenoteMuteRecord(
            accountId = 0L,
            userId = "user-id-1",
            createdAt = createdAt,
            postedAt = postedAt
        )

        Assertions.assertEquals(expect, RenoteMuteRecord.from(model))
    }

    @Test
    fun from_GiveNullPostedAt() {
        val createdAt = Clock.System.now()

        val model = RenoteMute(
            User.Id(0L, "user-id-1"),
            createdAt = createdAt,
            postedAt = null
        )

        val expect = RenoteMuteRecord(
            accountId = 0L,
            userId = "user-id-1",
            createdAt = createdAt,
            postedAt = null
        )

        Assertions.assertEquals(expect, RenoteMuteRecord.from(model))
    }
}