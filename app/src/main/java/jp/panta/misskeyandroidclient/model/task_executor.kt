package jp.panta.misskeyandroidclient.model

import kotlinx.coroutines.CoroutineScope
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

    fun<T> dispatch(task: ITask<T>, isLazy: Boolean = false): Flow<TaskState>
}

class AppTaskExecutor(
    val coroutineScope: CoroutineScope
) : TaskExecutor {

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
        }.shareIn(coroutineScope, if(isLazy) SharingStarted.Lazily else SharingStarted.Eagerly)

    }

}