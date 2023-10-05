package net.pantasystem.milktea.channel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.paginator.PreviousPagingController
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.channel.ChannelListType
import net.pantasystem.milktea.data.infrastructure.channel.ChannelPagingModel
import net.pantasystem.milktea.model.account.Account
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
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val EXTRA_SPECIFIED_ACCOUNT_ID = "ChannelViewModel.EXTRA_SPECIFIED_ACCOUNT_ID"
        const val EXTRA_ADD_TAB_TO_ACCOUNT_ID = "ChannelViewModel.EXTRA_ADD_TAB_TO_ACCOUNT_ID"
    }

    val logger: Logger by lazy {
        loggerFactory.create("ChannelViewModel")
    }


    private val featuredChannelPagingModel =
        channelPagingModelFactory.create(ChannelListType.FEATURED) {
            getAccount()
        }

    private val followedChannelPagingModel =
        channelPagingModelFactory.create(ChannelListType.FOLLOWED) {
            getAccount()
        }

    private val ownedChannelPagingModel = channelPagingModelFactory.create(ChannelListType.OWNED) {
        getAccount()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentAccount = savedStateHandle.getStateFlow<Long?>(
        EXTRA_SPECIFIED_ACCOUNT_ID,
        null
    ).flatMapLatest { accountId ->
        accountStore.getOrCurrent(accountId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val tabToAddAccount = savedStateHandle.getStateFlow<Long?>(
        EXTRA_ADD_TAB_TO_ACCOUNT_ID,
        null
    ).flatMapLatest { accountId ->
        accountStore.getOrCurrent(accountId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiState = combine(
        featuredChannelPagingModel.observeChannels(),
        followedChannelPagingModel.observeChannels(),
        ownedChannelPagingModel.observeChannels(),
        currentAccount,
        tabToAddAccount,
    ) { featured, followed, owned, currentAccount, tabToAddAccount ->
        ChannelListUiState.from(
            featured = featured,
            followed = followed,
            owned = owned,
            currentAccount = currentAccount,
            tabToAddAccount = tabToAddAccount,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ChannelListUiState()
    )


    fun clearAndLoad(type: ChannelListType) {
        viewModelScope.launch {
            val model = when (type) {
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

    fun loadOld(type: ChannelListType) {
        viewModelScope.launch {
            val model = when (type) {
                ChannelListType.OWNED -> ownedChannelPagingModel
                ChannelListType.FOLLOWED -> followedChannelPagingModel
                ChannelListType.FEATURED -> featuredChannelPagingModel
            }

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
                val account = getAddTabToAccount()
                val channel = channelRepository.findOne(channelId).getOrThrow()
                val relatedAccount = accountRepository.get(channel.id.accountId).getOrThrow()
                val page = account.newPage(
                    Pageable.ChannelTimeline(channelId = channelId.channelId),
                    channel.name,
                ).copy(
                    attachedAccountId = getSpecifiedAccountId(),
                    title = if (account.accountId == relatedAccount.accountId) {
                        channel.name
                    } else {
                        "${channel.name}(${relatedAccount.getAcct()})"
                    }
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

    //
//    fun setSpecifiedAccountId(accountId: Long) {
//        savedStateHandle[EXTRA_SPECIFIED_ACCOUNT_ID] = accountId
//    }
//
//    fun setAddTabToAccountId(accountId: Long) {
//        savedStateHandle[EXTRA_ADD_TAB_TO_ACCOUNT_ID] = accountId
//    }
//
    private fun getSpecifiedAccountId(): Long? {
        return savedStateHandle[EXTRA_SPECIFIED_ACCOUNT_ID]
    }

    private fun getAddToTabAccountId(): Long? {
        return savedStateHandle[EXTRA_ADD_TAB_TO_ACCOUNT_ID]
    }

    private suspend fun getAccount(): Account {
        val accountId = getSpecifiedAccountId()
        if (accountId != null) {
            return accountRepository.get(accountId).getOrThrow()
        }
        return accountRepository.getCurrentAccount().getOrThrow()
    }

    private suspend fun getAddTabToAccount(): Account {
        val accountId = getAddToTabAccountId()
        if (accountId != null) {
            return accountRepository.get(accountId).getOrThrow()
        }
        return accountRepository.getCurrentAccount().getOrThrow()
    }
}

data class ChannelListUiState(
    val currentAccount: Account? = null,
    val featuredChannels: PageableState<List<ChannelListItem>> = PageableState.Loading.Init(),
    val followedChannels: PageableState<List<ChannelListItem>> = PageableState.Loading.Init(),
    val ownedChannels: PageableState<List<ChannelListItem>> = PageableState.Loading.Init(),
) {

    companion object {
        fun from(
            featured: PageableState<List<Channel>>,
            followed: PageableState<List<Channel>>,
            owned: PageableState<List<Channel>>,
            currentAccount: Account?,
            tabToAddAccount: Account?,
        ): ChannelListUiState {
            return ChannelListUiState(
                currentAccount = currentAccount,
                featuredChannels = featured.convert { list ->
                    list.map { channel ->
                        ChannelListItem(
                            channel,
                            isAddedTab = tabToAddAccount?.pages?.any { page ->
                                page.pageParams.channelId == channel.id.channelId
                                        && channel.id.accountId == (
                                        page.attachedAccountId ?: page.accountId)
                            } ?: false
                        )
                    }
                },
                followedChannels = followed.convert { list ->
                    list.map { channel ->
                        ChannelListItem(
                            channel,
                            isAddedTab = tabToAddAccount?.pages?.any { page ->
                                page.pageParams.channelId == channel.id.channelId
                                        && channel.id.accountId == (
                                        page.attachedAccountId ?: page.accountId)
                            } ?: false
                        )
                    }
                },
                ownedChannels = owned.convert { list ->
                    list.map { channel ->
                        ChannelListItem(
                            channel,
                            isAddedTab = tabToAddAccount?.pages?.any { page ->
                                page.pageParams.channelId == channel.id.channelId
                                        && channel.id.accountId == (
                                        page.attachedAccountId ?: page.accountId)
                            } ?: false
                        )

                    }
                },
            )
        }
    }

    fun getByType(type: ChannelListType): PageableState<List<ChannelListItem>> {
        return when (type) {
            ChannelListType.OWNED -> ownedChannels
            ChannelListType.FOLLOWED -> followedChannels
            ChannelListType.FEATURED -> featuredChannels
        }
    }
}

data class ChannelListItem(
    val channel: Channel,
    val isAddedTab: Boolean,
)
