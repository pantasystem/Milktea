package jp.panta.misskeyandroidclient.viewmodel.tags

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.pantasystem.milktea.api.misskey.hashtag.RequestHashTagList
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.hashtag.HashTag
import java.io.Serializable

class SortedHashTagListViewModel @AssistedInject constructor(
    val misskeyAPIProvider: MisskeyAPIProvider,
    val accountStore: AccountStore,
    val encryption: Encryption,
    @Assisted val conditions: Conditions
) : ViewModel() {

    @AssistedFactory
    interface AssistedViewModelFactory {
        fun create(conditions: Conditions): SortedHashTagListViewModel
    }

    data class Conditions(
        val sort: String,
        val isAttachedToUserOnly: Boolean? = null,
        val isAttachedToLocalUserOnly: Boolean? = null,
        val isAttachedToRemoteUserOnly: Boolean? = null
    ) : Serializable

    companion object

    val hashTags = object : MediatorLiveData<List<HashTag>>() {

        override fun onActive() {
            super.onActive()

            if (value.isNullOrEmpty()) {
                load()
            }
        }

    }

    val isLoading = MutableLiveData<Boolean>()

    init {
        accountStore.observeCurrentAccount.filterNotNull().flowOn(Dispatchers.IO)
            .onEach {
                load()
            }.launchIn(viewModelScope)
    }

    fun load() {
        val account = accountStore.currentAccount
            ?: return
        isLoading.value = true
        val i = runCatching { account.getI(encryption) }.getOrNull()
        if (i == null) {
            isLoading.value = false
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                misskeyAPIProvider.get(account).getHashTagList(
                    RequestHashTagList(
                        i = i,
                        sort = conditions.sort
                    )
                ).throwIfHasError()
            }.onSuccess { response ->
                hashTags.postValue(response.body())
            }
            isLoading.postValue(false)
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun SortedHashTagListViewModel.Companion.provideViewModel(
    factory: SortedHashTagListViewModel.AssistedViewModelFactory,
    conditions: SortedHashTagListViewModel.Conditions
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(conditions) as T
    }
}