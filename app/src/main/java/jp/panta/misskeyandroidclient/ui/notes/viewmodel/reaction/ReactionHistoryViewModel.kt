package jp.panta.misskeyandroidclient.ui.notes.viewmodel.reaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryPaginator
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class ReactionHistoryViewModel(
    private val reactionHistoryDataSource: net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource,
    private val paginator: net.pantasystem.milktea.model.notes.reaction.ReactionHistoryPaginator,
    val logger: Logger?
) : ViewModel(){


    @Suppress("UNCHECKED_CAST")
    class Factory(
        val noteId: net.pantasystem.milktea.model.notes.Note.Id,
        val type: String?,
        val miCore: MiCore
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReactionHistoryViewModel(
                miCore.getReactionHistoryDataSource(),
                miCore.getReactionHistoryPaginatorFactory().create(
                    net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest(
                        noteId,
                        type
                    )
                ),
                miCore.loggerFactory.create("ReactionHistoryVM")
            ) as T
        }
    }

    val isLoading = MutableLiveData(false)
    val histories = MutableLiveData<List<net.pantasystem.milktea.model.notes.reaction.ReactionHistory>>(emptyList())

    init {
        reactionHistoryDataSource.filter(paginator.reactionHistoryRequest.noteId, paginator.reactionHistoryRequest.type).onEach {
            histories.postValue(it)
        }.catch {

        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun next() {
        if(isLoading.value == true) {
            return
        }
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {

            runCatching {
                paginator.next()
            }.onFailure {
                logger?.error("リアクションの履歴の取得に失敗しました", e = it)
            }
            isLoading.postValue(false)
        }
    }

}