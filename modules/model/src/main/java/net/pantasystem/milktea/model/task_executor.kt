package net.pantasystem.milktea.model


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.gallery.GalleryPost

/**
 * アプリケーションのスコープの範囲内で完了するタスク
 */
@Deprecated("複雑でわかりづらいのでWorkerかStoreに移行したい")
fun interface ITask<T> {
    suspend fun execute(): T
}

@Deprecated("複雑でわかりづらいのでWorkerかStoreに移行したい")
sealed class TaskState<T> {
    data class Success<T>(val res: T) : TaskState<T>()
    data class Error<T>(val e: Throwable, val task: ITask<T>) : TaskState<T>()
    class Executing<T> : TaskState<T>()
}

/**
 * 別のCoroutineScopeでタスクを実行する
 */
@Deprecated("複雑でわかりづらいのでWorkerかStoreに移行したい")
interface TaskExecutor<T> {
    val tasks: Flow<TaskState<T>>
    fun dispatch(task: ITask<T>, isLazy: Boolean = false): Flow<TaskState<T>>
}

@Deprecated("複雑でわかりづらいのでWorkerかStoreに移行したい")
class TaskExecutorImpl<T>(
    val coroutineScope: CoroutineScope,
    val logger: Logger
) : TaskExecutor<T> {

    private val _tasks = MutableSharedFlow<TaskState<T>>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 100
    )
    override val tasks: Flow<TaskState<T>> = _tasks

    override fun dispatch(task: ITask<T>, isLazy: Boolean): Flow<TaskState<T>> {
        return flow<TaskState<T>> {
            runCancellableCatching {
                emit(TaskState.Executing())
                task.execute()
            }.onSuccess {
                emit(TaskState.Success(it))
            }.onFailure {
                logger.debug("タスクの実行中にエラーが発生しました", e = it)
                emit(TaskState.Error(it, task))
            }
        }.onEach {
            _tasks.tryEmit(it)
        }.shareIn(coroutineScope, if (isLazy) SharingStarted.Lazily else SharingStarted.Eagerly)

    }
}

@Deprecated("複雑でわかりづらいのでWorkerかStoreに移行したい")
class CreateGalleryTaskExecutor(
    private val taskExecutorImpl: TaskExecutor<GalleryPost>
) : TaskExecutor<GalleryPost> by taskExecutorImpl