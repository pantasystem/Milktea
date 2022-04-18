package jp.panta.misskeyandroidclient.streaming.network

import jp.panta.misskeyandroidclient.logger.TestLogger
import net.pantasystem.milktea.data.streaming.Socket
import net.pantasystem.milktea.data.streaming.StreamingEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.data.model.streaming.stateEvent
import net.pantasystem.milktea.data.streaming.network.SocketImpl
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test

class SocketImplTest {

    @Test
    fun testBlockingConnect() {
        val wssURL = "wss://misskey.io/streaming"
        val logger = TestLogger.Factory()
        val okHttpClient = OkHttpClient()
        val socket = SocketImpl(wssURL, okHttpClient, logger)
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
        val okHttpClient = OkHttpClient()
        val socket = SocketImpl(wssURL, okHttpClient, logger)

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
        val okHttpClient = OkHttpClient()
        val socket = SocketImpl(wssURL, okHttpClient, logger)

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