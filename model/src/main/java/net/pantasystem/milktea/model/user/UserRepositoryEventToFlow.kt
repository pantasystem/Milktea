package net.pantasystem.milktea.model.user


import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.Logger

/**
 * UserRepositoryのイベントをFlowに変換する
 */
class UserRepositoryEventToFlow(
    private val userDataSource: UserDataSource,
    val coroutineScope: CoroutineScope,
    val loggerFactory: Logger.Factory,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    buffer: Int = 1000
) : UserDataSource.Listener {

    val logger = loggerFactory.create("UserRepositoryEventToFlow")

    private val flow = MutableSharedFlow<UserDataSource.Event>(extraBufferCapacity = buffer)

    init {
        userDataSource.addEventListener(this)
        coroutineScope.launch(dispatcher) {
            (flow.subscriptionCount as Flow<Int>).distinctUntilChanged().collect {
                if(it == 0) {
                    userDataSource.removeEventListener(this@UserRepositoryEventToFlow)
                }else{
                    userDataSource.addEventListener(this@UserRepositoryEventToFlow)
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun from(userId: User.Id): Flow<UserDataSource.Event> {
        return flow.filter {
            it.userId == userId
        }
    }



    override fun on(e: UserDataSource.Event) {
        if(!flow.tryEmit(e)) {
            logger.warning("emitをキャンセルされました:$e")
        }
    }

}