package jp.panta.misskeyandroidclient.model.channel

import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.channel.ChannelState
import net.pantasystem.milktea.model.user.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class ChannelStateTest {

    @Test
    fun add() {
        var state = ChannelState(emptyMap())
        val channel = generateChannel(Channel.Id(0, "id"), "name")
        state = state.add(channel)
        assertEquals(1, state.channels.size)

    }

    @Test
    fun addAll() {
        var state = ChannelState(
            (0 until 10).associate {
                Channel.Id(0, "id$it") to generateChannel(Channel.Id(0, "id$it"), it.toString())
            }
        )

        val channels = (0 until 10).map {
            generateChannel(Channel.Id(0, "id-$it"), it.toString())
        }
        state = state.addAll(channels)
        assertEquals(20, state.channels.size)
    }

    @Test
    fun remove() {
        var state = ChannelState(
            (0 until 10).associate {
                Channel.Id(0, "id$it") to generateChannel(Channel.Id(0, "id$it"), it.toString())
            }
        )

        assertNotNull(state.get(Channel.Id(0, "id0")))
        state = state.remove(Channel.Id(0, "id0"))
        assertNull(state.get(Channel.Id(0, "id0")))
    }

    @Test
    fun get() {
        var state = ChannelState(emptyMap())
        val channel = generateChannel(Channel.Id(0, "id"), "name")
        state = state.add(channel)
        state = state.add(generateChannel(Channel.Id(0, "id2"), "name"))
        assertEquals(channel, state.get(channel.id))
    }

    @Test
    fun getIn() {
        val state = ChannelState(
            (0 until 10).associate {
                Channel.Id(0, "id$it") to generateChannel(Channel.Id(0, "id$it"), it.toString())
            }
        )
        val ids = listOf(
            Channel.Id(0, "id0"),
            Channel.Id(0, "id2")
        )

        val list = state.getIn(ids)
        assertEquals(2, list.size)
        assertTrue(list.any { it.id == ids[0] })
        assertTrue(list.any { it.id == ids[1] })

    }


    private fun generateChannel(id: Channel.Id, name: String): Channel {
        return Channel(
            id = id,
            name = name,
            description = null,
            bannerUrl = null,
            createdAt = Clock.System.now(),
            hasUnreadNote = null,
            isFollowing = null,
            lastNotedAt = null,
            notesCount = 0,
            usersCount = 0,
            userId = User.Id(id.accountId, "userId")
        )
    }

}