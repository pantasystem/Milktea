package jp.panta.misskeyandroidclient.ui.users.viewmodel.search

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.UserViewData
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.util.asLoadingStateFlow
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import java.util.regex.Pattern
import javax.inject.Inject


data class SearchUser(
    val word: String,
    val host: String?
) {
    val isUserName: Boolean
        get() = Pattern.compile("""^[a-zA-Z_\-0-9]+$""")
            .matcher(word)
            .find()

}
/**
 * SearchAndSelectUserViewModelを将来的にこのSearchUserViewModelと
 * SelectedUserViewModelに分離する予定
 */
@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class SearchUserViewModel @Inject constructor(
    val miCore: MiCore,
) : ViewModel(){

    private val logger = miCore.loggerFactory.create("SearchUserViewModel")


    private val searchUserRequests = MutableSharedFlow<SearchUser>(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 25
    )

    val userName = MutableLiveData<String>()
    val host = MutableLiveData<String>()

    private val searchState = miCore.getAccountStore().observeCurrentAccount.filterNotNull()
        .flatMapLatest { account ->
            searchUserRequests.distinctUntilChanged()
                .flatMapLatest {
                    suspend {
                        if (it.isUserName) {
                            miCore.getUserRepository()
                                .searchByUserName(
                                    accountId = account.accountId,
                                    userName = it.word,
                                    host = it.host
                                )
                        } else {
                            miCore.getUserRepository()
                                .searchByName(
                                    accountId = account.accountId,
                                    name = it.word
                                )
                        }
                    }.asLoadingStateFlow()
                }
        }.flowOn(Dispatchers.IO)
        .onEach {
            logger.debug("検索状態:$it")
        }
        .catch { error ->
            logger.info("ユーザー検索処理に失敗しました", e = error)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, State.Fixed(StateContent.NotExist()))

    val isLoading = searchState.map {
        it is State.Loading
    }.asLiveData()

    val users = searchState.map {
        (it.content as? StateContent.Exist)?.rawContent
            ?: emptyList()
    }.map {
        it.map { u ->
            UserViewData(u, miCore, viewModelScope)
        }
    }.asLiveData()

    init {
        userName.observeForever {
            search()
        }

        host.observeForever {
            search()
        }
    }

    fun search(){
        val userName = this.userName.value?: return
        val host = this.host.value

        val request = SearchUser(
            host = host,
            word = userName
        )
        searchUserRequests.tryEmit(request)
    }

}