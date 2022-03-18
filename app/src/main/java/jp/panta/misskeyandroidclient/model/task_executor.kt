package jp.panta.misskeyandroidclient.model

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.gallery.GalleryPost
import jp.panta.misskeyandroidclient.model.notes.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

/**
 * アプリケーションのスコープの範囲内で完了するタスク
 */
fun interface ITask<T> {
    suspend fun execute(): T
}

sealed class TaskState<T> {
    data class Success<T>(val res: T) : TaskState<T>()
    data class Error<T>(val e: Throwable) : TaskState<T>()
    class Executing<T> : TaskState<T>()
}

/**
 * 別のCoroutineScopeでタスクを実行する
 */
interface TaskExecutor<T> {
    val tasks: Flow<TaskState<T>>
    fun dispatch(task: ITask<T>, isLazy: Boolean = false): Flow<TaskState<T>>
}

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
            runCatching {
                emit(TaskState.Executing())
                task.execute()
            }.onSuccess {
                emit(TaskState.Success(it))
            }.onFailure {
                logger.debug("タスクの実行中にエラーが発生しました", e = it)
                emit(TaskState.Error(it))
            }
        }.onEach {
            _tasks.tryEmit(it)
        }.shareIn(coroutineScope, if (isLazy) SharingStarted.Lazily else SharingStarted.Eagerly)

    }
}
class CreateNoteTaskExecutor(
    private val taskExecutorImpl: TaskExecutor<Note>
) : TaskExecutor<Note> by taskExecutorImpl

class CreateGalleryTaskExecutor(
    private val taskExecutorImpl: TaskExecutor<GalleryPost>
) : TaskExecutor<GalleryPost> by taskExecutorImpl