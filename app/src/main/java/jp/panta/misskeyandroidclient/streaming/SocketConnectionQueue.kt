package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.filter

class SocketConnectionQueue(
    private val socketWithAccountProvider: SocketWithAccountProvider,
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    loggerFactory: Logger.Factory
) {
    private val logger = loggerFactory.create("SocketConnectionQueue")
    private val scope = coroutineScope + dispatcher
    private val connectQueueMap = mutableMapOf<Socket, MutableSharedFlow<Unit>>()
    private var penaltyPoint = mapOf<Socket, Int>()

    suspend fun connect(account: Account, force: Boolean = true) {
        val socket = socketWithAccountProvider.get(account)
        connect(socket, force)
    }

    /**
     * @param force trueの場合はリミットを無視して接続します
     */
    suspend fun connect(socket: Socket, force: Boolean = true) {
        if(force) {
            if(socket.state() != Socket.State.Connected) {
                socket.blockingConnect()
                penaltyPoint = penaltyPoint.toMutableMap().also {
                    it[socket] = 0
                }
            }
            return
        }
        val queue: MutableSharedFlow<Unit>
        = synchronized(connectQueueMap) {
            var queue = connectQueueMap[socket]
            if(queue != null){
                return@synchronized queue
            }
            queue = MutableSharedFlow(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            connectQueueMap[socket] = queue
            queue.filter {
                socket.state() != Socket.State.Connected
            }.onEach {
                logger.debug("接続を試みた")
                if(!socket.blockingConnect().also {
                    logger.debug("接続要求可否 : ${if(it) "ok" else "failure"}")
                }){
                    val point = penaltyPoint[socket]?: 0
                    if(point > 0){
                        delay(point * 100L)
                    }
                    penaltyPoint = penaltyPoint.toMutableMap().also {
                        it[socket] = point + 1
                    }
                }


            }.catch { e ->
                logger.error("connect試行中にエラー発生", e = e)
            }.launchIn(scope)
            queue
        }
        logger.debug(if(queue.tryEmit(Unit)) "QueueへのPushを失敗" else "queueへのPush成功")
    }
}