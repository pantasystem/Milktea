package net.pantasystem.milktea.note.reaction.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject


@HiltViewModel
class ReactionHistoryViewModel @Inject constructor(
    reactionHistoryDataSource: ReactionHistoryDataSource,
    paginatorFactory: ReactionHistoryPaginator.Factory,
    val loggerFactory: Logger.Factory,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val EXTRA_NOTE_ID = "ReactionHistoryViewModel.EXTRA_NOTE_ID"
        const val EXTRA_TYPE = "ReactionHistoryViewModel.TYPE"
    }

    val noteId: Note.Id = requireNotNull(savedStateHandle[EXTRA_NOTE_ID])
    val type: String? = savedStateHandle[EXTRA_TYPE]

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