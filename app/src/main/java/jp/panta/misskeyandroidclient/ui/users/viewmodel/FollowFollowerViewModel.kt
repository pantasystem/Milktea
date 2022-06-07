package jp.panta.misskeyandroidclient.ui.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.user.FollowFollowerPagingStore
import net.pantasystem.milktea.model.user.RequestType
import net.pantasystem.milktea.model.user.User

class FollowFollowerViewModel @AssistedInject constructor(
    followFollowerPagingStoreFactory: FollowFollowerPagingStore.Factory,
    @Assisted val type: RequestType
) : ViewModel(), ShowUserDetails {

    companion object

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(type: RequestType): FollowFollowerViewModel
    }

    private val followFollowerPagingStore = followFollowerPagingStoreFactory.create(type)

    val isInitialLoading = followFollowerPagingStore.state.map {
        it is PageableState.Loading.Init
    }

    val users = followFollowerPagingStore.users.asLiveData()

    fun loadInit() = viewModelScope.launch(Dispatchers.IO) {
        followFollowerPagingStore.clear()
        followFollowerPagingStore.loadPrevious()
    }


    fun loadOld() = viewModelScope.launch(Dispatchers.IO) {
        followFollowerPagingStore.loadPrevious()
    }


    val showUserEventBus = EventBus<User.Id>()

    override fun show(userId: User.Id?) {
        showUserEventBus.event = userId
    }


}

fun FollowFollowerViewModel.Companion.provideFactory(
    factory: FollowFollowerViewModel.ViewModelAssistedFactory,
    type: RequestType,
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return factory.create(type) as T
    }
}