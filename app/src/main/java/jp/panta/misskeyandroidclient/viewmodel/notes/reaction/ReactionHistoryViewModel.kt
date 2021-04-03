package jp.panta.misskeyandroidclient.viewmodel.notes.reaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistory
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDataSource
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryPaginator
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryRequest
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class ReactionHistoryViewModel(
    val reactionHistoryDataSource: ReactionHistoryDataSource,
    val paginator: ReactionHistoryPaginator,
    val logger: Logger?
) : ViewModel(){


    @Suppress("UNCHECKED_CAST")
    class Factory(
        val noteId: Note.Id,
        val type: String?,
        val miCore: MiCore
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ReactionHistoryViewModel(
                miCore.getReactionHistoryDataSource(),
                miCore.getReactionHistoryPaginatorFactory().create(
                    ReactionHistoryRequest(noteId, type)
                ),
                miCore.loggerFactory.create("ReactionHistoryVM")
            ) as T
        }
    }

    val isLoading = MutableLiveData(false)
    val histories = MutableLiveData<List<ReactionHistory>>(emptyList())

    init {
        reactionHistoryDataSource.filterByNoteId(paginator.reactionHistoryRequest.noteId).onEach {
            histories.postValue(it)
        }.catch { e ->

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