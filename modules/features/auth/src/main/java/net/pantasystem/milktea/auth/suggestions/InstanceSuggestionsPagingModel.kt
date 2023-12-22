package net.pantasystem.milktea.auth.suggestions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.milktea.instance.ticker.InstanceTickerAPIServiceBuilder
import net.pantasystem.milktea.api.misskey.infos.SimpleInstanceInfo
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.paginator.EntityConverter
import net.pantasystem.milktea.common.paginator.PaginationState
import net.pantasystem.milktea.common.paginator.PreviousLoader
import net.pantasystem.milktea.common.paginator.PreviousPagingController
import net.pantasystem.milktea.common.paginator.StateLocker
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import javax.inject.Inject

class InstanceSuggestionsPagingModel @Inject constructor(
    private val instanceTickerAPIBuilder: InstanceTickerAPIServiceBuilder,
    private val loggerFactory: Logger.Factory,
) : StateLocker,
    PaginationState<SimpleInstanceInfo>,
    PreviousLoader<SimpleInstanceInfo>,
    EntityConverter<SimpleInstanceInfo, SimpleInstanceInfo> {

    private val logger by lazy {
        loggerFactory.create("InstanceSuggestionsPagingModel")
    }
    private var _offset = 0
    private var _name: String = ""
    private val _state =
        MutableStateFlow<PageableState<List<SimpleInstanceInfo>>>(PageableState.Loading.Init())

    private var _job: Job? = null

    override suspend fun convertAll(list: List<SimpleInstanceInfo>): List<SimpleInstanceInfo> {
        return list
    }

    override val state: Flow<PageableState<List<SimpleInstanceInfo>>>
        get() = _state

    override fun getState(): PageableState<List<SimpleInstanceInfo>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<SimpleInstanceInfo>>) {
        _state.value = state
    }

    override suspend fun loadPrevious(): Result<List<SimpleInstanceInfo>> =
        runCancellableCatching {
            instanceTickerAPIBuilder.build("https://milktea-instance-ticker.milktea.workers.dev/").getInstances(
                offset = _offset,
                name = _name,
//                lang = Locale.current.language,
            ).throwIfHasError().body()!!.also {
                _offset += it.size
            }
        }

    suspend fun setQueryName(name: String) {
        _job?.cancel()
        mutex.withLock {
            _name = name
            _offset = 0
        }
        setState(PageableState.Loading.Init())
    }

    override val mutex: Mutex = Mutex()

    private val previousPagingController = PreviousPagingController.create(
        this,
    )

    fun onLoadNext(scope: CoroutineScope) {
        _job?.cancel()
        _job = scope.launch {
            previousPagingController.loadPrevious().onFailure {
                logger.error("Failed to load previous", it)
            }
        }

    }
}