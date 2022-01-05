package jp.panta.misskeyandroidclient.streaming.network

import jp.panta.misskeyandroidclient.logger.TestLogger
import jp.panta.misskeyandroidclient.streaming.Socket
import jp.panta.misskeyandroidclient.streaming.StreamingEvent
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
            val res: Socket.State = suspendCoroutine { continuation ->
                var flag = true
                socket.addStateEventListener { ev ->
                    if(ev == Socket.State.Connected && flag) {
                        continuation.resume(ev)
                        flag = false
                    }
                }

            }
            assertEquals(Socket.State.Connected, res)

        }
    }

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
            val res: Socket.State = suspendCoroutine { continuation ->
                var flag = true
                socket.addStateEventListener { ev ->
                    if(ev is Socket.State.Connected && flag) {
                        continuation.resume(ev)
                        flag = false
                    }
                }

            }
            assertTrue(res is Socket.State.Connected)

            socket.removeMessageEventListener(listener)
            val closedRes: Socket.State = suspendCoroutine { continuation ->
                var flag = true
                socket.addStateEventListener { ev ->
                    if(ev is Socket.State.Closed && flag) {
                        continuation.resume(ev)
                        flag = false
                    }
                }

            }
            assertTrue(closedRes is Socket.State.Closed)

        }
    }

}