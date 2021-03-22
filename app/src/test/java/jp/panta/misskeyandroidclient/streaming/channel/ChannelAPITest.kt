package jp.panta.misskeyandroidclient.streaming.channel

import jp.panta.misskeyandroidclient.logger.TestLogger
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.network.SocketImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*

class ChannelAPITest {

    @ExperimentalCoroutinesApi
    @Test
    fun connect(): Unit = runBlocking {
        val wssURL = "wss://misskey.io/streaming"
        val logger = TestLogger.Factory()
        val okHttpClient = OkHttpClient()
        val socket = SocketImpl(wssURL, okHttpClient, { true }, logger)
        socket.blockingConnect()

        var count = 0
        launch {
            ChannelAPI(socket, logger).connect(ChannelAPI.Type.GLOBAL).collect {
                println(it)
                assertTrue(it is ChannelBody.ReceiveNote)
                count ++
                if(count > 5) {
                    cancel()
                }
            }
        }.join()


    }
}