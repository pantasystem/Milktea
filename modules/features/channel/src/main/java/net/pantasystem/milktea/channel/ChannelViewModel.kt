package net.pantasystem.milktea.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.paginator.PreviousPagingController
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.channel.ChannelListType
import net.pantasystem.milktea.data.infrastructure.channel.ChannelPagingModel
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.account.page.newPage
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.channel.ChannelRepository
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    val accountStore: AccountStore,
    private val channelRepository: ChannelRepository,
    private val accountRepository: AccountRepository,
    channelPagingModelFactory: ChannelPagingModel.ModelAssistedFactory,
    loggerFactory: Logger.Factory,
) : ViewModel() {

    val logger: Logger by lazy {
        loggerFactory.create("ChannelViewModel")
    }

    private val channelPagingModelHolder = ChannelPagingModelHolder(channelPagingModelFactory)


    @OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun getObservable(key: PagingModelKey): Flow<PageableState<List<Channel>>> {
        return suspend {
            channelPagingModelHolder.get(key)
        }.asFlow().flatMapLatest {
            it.observeChannels()
        }
    }


    fun clearAndLoad(key: PagingModelKey) {
        viewModelScope.launch {
            val model = channelPagingModelHolder.get(key)
            model.clear()
            PreviousPagingController(
                model,
                model,
                model,
                model,
            ).loadPrevious()
        }
    }

    fun follow(channelId: Channel.Id) {
        viewModelScope.launch {
            runCancellableCatching {
                channelRepository.follow(channelId)
            }.onFailure {
                logger.info("follow error:$channelId", e = it)
            }
        }
    }

    fun unFollow(channelId: Channel.Id) {
        viewModelScope.launch {
            runCancellableCatching {
                channelRepository.unFollow(channelId)
            }.onFailure {
                logger.info("unFollow error:$channelId", e = it)
            }
        }
    }

    fun toggleTab(channelId: Channel.Id) {
        viewModelScope.launch {
            runCancellableCatching {
                val account = accountRepository.get(channelId.accountId).getOrThrow()
                val channel = channelRepository.findOne(channelId).getOrThrow()
                val page = account.newPage(
                    Pageable.ChannelTimeline(channelId = channelId.channelId),
                    channel.name
                )
                val first =
                    account.pages.firstOrNull { (it.pageable() as? Pageable.ChannelTimeline)?.channelId == channelId.channelId }
                if (first == null) {
                    accountStore.addPage(page)
                } else {
                    accountStore.removePage(first)
                }
            }
        }
    }
}

data class PagingModelKey(val accountId: Long, val type: ChannelListType)

class ChannelPagingModelHolder(
    private val channelPagingModelFactory: ChannelPagingModel.ModelAssistedFactory,
) {

    private val lock = Mutex()
    private var modelMap = mapOf<PagingModelKey, ChannelPagingModel>()
    suspend fun get(key: PagingModelKey): ChannelPagingModel {
        lock.withLock {
            var model = modelMap[key]
            if (model == null) {
                model = channelPagingModelFactory.create(key.accountId, key.type)
                modelMap = modelMap.toMutableMap().also {
                    it[key] = model
                }
            }
            return model
        }
    }
}