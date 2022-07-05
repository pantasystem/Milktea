package jp.panta.misskeyandroidclient.ui.notes.viewmodel.reaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryPaginator
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest


class ReactionHistoryViewModel @AssistedInject constructor(
    reactionHistoryDataSource: ReactionHistoryDataSource,
    paginatorFactory: ReactionHistoryPaginator.Factory,
    val loggerFactory: Logger.Factory,
    @Assisted val noteId: Note.Id,
    @Assisted val type: String?
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(noteId: Note.Id, type: String?): ReactionHistoryViewModel
    }

    companion object

    val logger = loggerFactory.create("ReactionHistoryVM")

    val isLoading = MutableLiveData(false)
    val histories = MutableLiveData<List<ReactionHistory>>(emptyList())
    private val paginator = paginatorFactory.create(ReactionHistoryRequest(noteId, type))

    init {
        reactionHistoryDataSource.filter(noteId, type).onEach {
            histories.postValue(it)
        }.catch {

        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun next() {
        if (isLoading.value == true) {
            return
        }
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {

            runCatching {
                paginator.next()
            }.onFailure {
                logger.error("リアクションの履歴の取得に失敗しました", e = it)
            }
            isLoading.postValue(false)
        }
    }

}

@Suppress("UNCHECKED_CAST")
fun ReactionHistoryViewModel.Companion.provideViewModel(
    factory: ReactionHistoryViewModel.ViewModelAssistedFactory,
    noteId: Note.Id,
    type: String?
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(noteId, type) as T
    }
}