package jp.panta.misskeyandroidclient.ui.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.panta.misskeyandroidclient.model.FuturePagingController
import jp.panta.misskeyandroidclient.model.PaginationState
import jp.panta.misskeyandroidclient.model.PreviousPagingController
import jp.panta.misskeyandroidclient.model.account.AccountStore
import jp.panta.misskeyandroidclient.model.channel.Channel
import jp.panta.misskeyandroidclient.model.channel.ChannelListType
import jp.panta.misskeyandroidclient.model.channel.ChannelPagingModel
import jp.panta.misskeyandroidclient.util.PageableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    val accountStore: AccountStore,
    channelPagingModelFactory: ChannelPagingModel.ModelAssistedFactory,
) : ViewModel() {


    private val channelPagingModelHolder = ChannelPagingModelHolder(channelPagingModelFactory)


    @OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun getObservable(key: PagingModelKey): Flow<PageableState<List<Channel>>> {
        return suspend {
            channelPagingModelHolder.get(key)
        }.asFlow().flatMapLatest {
            it.observeChannels()
        }
    }


    fun loadPrevious(key: PagingModelKey) {
        viewModelScope.launch(Dispatchers.IO) {
            val model = channelPagingModelHolder.get(key)
            PreviousPagingController(
                model,
                model,
                model,
                model,
            ).loadPrevious()
        }
    }

    fun loadFuture(key: PagingModelKey) {
        viewModelScope.launch(Dispatchers.IO) {
            val model = channelPagingModelHolder.get(key)
            FuturePagingController(
                model,
                model,
                model,
                model,
            ).loadFuture()
        }
    }


    init {

        accountStore.observeCurrentAccount.filterNotNull().onEach {

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