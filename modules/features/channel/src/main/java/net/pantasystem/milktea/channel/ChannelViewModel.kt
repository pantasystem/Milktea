package net.pantasystem.milktea.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    channelPagingModelFactory: ChannelPagingModel.Factory,
    loggerFactory: Logger.Factory,
) : ViewModel() {

    val logger: Logger by lazy {
        loggerFactory.create("ChannelViewModel")
    }


    private val featuredChannelPagingModel =
        channelPagingModelFactory.create(ChannelListType.FEATURED) {
            accountRepository.getCurrentAccount().getOrThrow()
        }

    private val followedChannelPagingModel =
        channelPagingModelFactory.create(ChannelListType.FOLLOWED) {
            accountRepository.getCurrentAccount().getOrThrow()
        }

    private val ownedChannelPagingModel = channelPagingModelFactory.create(ChannelListType.OWNED) {
        accountRepository.getCurrentAccount().getOrThrow()
    }

    val uiState = combine(
        featuredChannelPagingModel.observeChannels(),
        followedChannelPagingModel.observeChannels(),
        ownedChannelPagingModel.observeChannels()
    ) { featured, followed, owned ->
        ChannelListUiState(
            featuredChannels = featured,
            followedChannels = followed,
            ownedChannels = owned,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ChannelListUiState()
    )



    fun clearAndLoad(type: ChannelListType) {
        viewModelScope.launch {
            val model = when(type) {
                ChannelListType.OWNED -> ownedChannelPagingModel
                ChannelListType.FOLLOWED -> followedChannelPagingModel
                ChannelListType.FEATURED -> featuredChannelPagingModel
            }
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

data class ChannelListUiState(
    val featuredChannels: PageableState<List<Channel>> = PageableState.Loading.Init(),
    val followedChannels: PageableState<List<Channel>> = PageableState.Loading.Init(),
    val ownedChannels: PageableState<List<Channel>> = PageableState.Loading.Init(),
) {
    fun getByType(type: ChannelListType): PageableState<List<Channel>> {
        return when(type) {
            ChannelListType.OWNED -> ownedChannels
            ChannelListType.FOLLOWED -> followedChannels
            ChannelListType.FEATURED -> featuredChannels
        }
    }
}

