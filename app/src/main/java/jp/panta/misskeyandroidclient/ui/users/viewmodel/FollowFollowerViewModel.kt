package jp.panta.misskeyandroidclient.ui.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.pantasystem.milktea.model.user.FollowFollowerPagingStore
import net.pantasystem.milktea.model.user.RequestType

class FollowFollowerViewModel @AssistedInject constructor(
    followFollowerPagingStoreFactory: FollowFollowerPagingStore.Factory,
    @Assisted val type: RequestType
) : ViewModel() {

    companion object

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(type: RequestType): FollowFollowerViewModel
    }

    private val followFollowerPagingStore = followFollowerPagingStoreFactory.create(type)


    val users = followFollowerPagingStore.users.asLiveData()
    val state = followFollowerPagingStore.state



    fun loadInit() = viewModelScope.launch(Dispatchers.IO) {
        followFollowerPagingStore.clear()
        followFollowerPagingStore.loadPrevious()
    }


    fun loadOld() = viewModelScope.launch(Dispatchers.IO) {
        followFollowerPagingStore.loadPrevious()
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