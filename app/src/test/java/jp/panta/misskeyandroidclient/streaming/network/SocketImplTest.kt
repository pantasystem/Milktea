package jp.panta.misskeyandroidclient.streaming.network

import jp.panta.misskeyandroidclient.logger.TestLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.api.misskey.DefaultOkHttpClientProvider
import net.pantasystem.milktea.api_streaming.Socket
import net.pantasystem.milktea.api_streaming.StreamingEvent
import net.pantasystem.milktea.api_streaming.network.SocketImpl
import net.pantasystem.milktea.data.infrastructure.streaming.stateEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class SocketImplTest {

    @Test
    fun testBlockingConnect() {
        val wssURL = "wss://misskey.io/streaming"
        val logger = TestLogger.Factory()
        val socket = SocketImpl(wssURL, logger, DefaultOkHttpClientProvider())
        runBlocking {
            socket.blockingConnect()
            assertEquals(socket.state(), Socket.State.Connected)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testAddMessageListener() {

        val wssURL = "wss://misskey.io/streaming"
        val logger = TestLogger.Factory()
        val socket = SocketImpl(wssURL, logger, DefaultOkHttpClientProvider())

        runBlocking {

            socket.addMessageEventListener {
                false
            }
            val res = socket.stateEvent().first {
                it == Socket.State.Connected
            }

            assertEquals(Socket.State.Connected, res)

        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testRemoveMessageListener() {
        val wssURL = "wss://misskey.io/streaming"
        val logger = TestLogger.Factory()
        val socket =
            SocketImpl(wssURL, logger, DefaultOkHttpClientProvider())

        runBlocking {

            val listener: (StreamingEvent)-> Boolean = {
                false
            }
            socket.addMessageEventListener(listener)
            val res: Socket.State = socket.stateEvent().first {
                it == Socket.State.Connected
            }
            assertTrue(res is Socket.State.Connected)

            socket.removeMessageEventListener(listener)
            val closedRes: Socket.State = socket.stateEvent().first {
                it is Socket.State.Closed
            }
            assertTrue(closedRes is Socket.State.Closed)

        }
    }

}