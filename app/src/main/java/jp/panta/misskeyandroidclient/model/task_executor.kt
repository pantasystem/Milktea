package jp.panta.misskeyandroidclient.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

/**
 * アプリケーションのスコープの範囲内で完了するタスク
 */
fun interface ITask<T> {
    suspend fun execute() : T
}

sealed class TaskState {
    data class Success<T>(val res: T) : TaskState()
    data class Error(val e: Throwable) : TaskState()
    object Executing : TaskState()
}

/**
 * 別のCoroutineScopeでタスクを実行する
 */
interface TaskExecutor {
    val tasks: Flow<TaskState>
    fun<T> dispatch(task: ITask<T>, isLazy: Boolean = false): Flow<TaskState>
}

class AppTaskExecutor(
    val coroutineScope: CoroutineScope
) : TaskExecutor {

    private val _tasks = MutableSharedFlow<TaskState>(onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 100)
    override val tasks: Flow<TaskState> = _tasks

    override fun <T> dispatch(task: ITask<T>, isLazy: Boolean) : Flow<TaskState>{
        return flow {
            runCatching {
                emit(TaskState.Executing)
                task.execute()
            }.onSuccess {
                emit(TaskState.Success(it))
            }.onFailure {
                emit(TaskState.Error(it))
            }
        }.onEach {
            _tasks.tryEmit(it)
        }.shareIn(coroutineScope, if(isLazy) SharingStarted.Lazily else SharingStarted.Eagerly)

    }

}