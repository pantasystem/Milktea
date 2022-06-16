package jp.panta.misskeyandroidclient.ui.notes.viewmodel.draft

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftNoteDao
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.notes.draft.DraftNote
import javax.inject.Inject

@HiltViewModel
class DraftNotesViewModel @Inject constructor(
    val draftNoteDao: DraftNoteDao,
    val accountStore: AccountStore,
    val loggerFactory: Logger.Factory,
) : ViewModel(){


    val logger = loggerFactory.create("DraftNotesVM")

    private val visibleContentDraftNoteIds: StateFlow<Set<Long>> = MutableStateFlow(emptySet())

    @OptIn(ExperimentalCoroutinesApi::class)
    val draftNotesState: StateFlow<ResultState<List<DraftNote>>> = accountStore.state.mapNotNull {
        it.currentAccountId
    }.flatMapLatest { currentAccountId ->
        draftNoteDao.observeDraftNotesRelation(currentAccountId).map { list ->
            list.map { relation ->
                relation.toDraftNote(currentAccountId)
            }
        }.map {
            @Suppress("USELESS_CAST")
            ResultState.Fixed(StateContent.Exist(it)) as ResultState<List<DraftNote>>
        }
    }.catch { e ->
        this.emit(ResultState.Error(StateContent.NotExist(), e))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ResultState.Loading(StateContent.NotExist()))

    val uiState = combine(visibleContentDraftNoteIds, draftNotesState) { ids, state ->
        DraftNotesPageUiState(
            visibleContentDraftNoteIds = ids,
            draftNotes = state
        )
    }




//    fun detachFile(file: File?) {
//
//        // TODO: 実装する
//        file?.localFileId?.let{
//            val notes = ArrayList(draftNotes.value?: emptyList())
//            val targetNote = (notes.firstOrNull { dNote ->
//                dNote.note.value?.files?.any {
//                    it.localFileId == file.localFileId
//                }?: false
//            }?: return).note.value ?: return
//
//            val updatedFiles = ArrayList(targetNote.files?: emptyList())
//            updatedFiles.remove(file)
//
//            viewModelScope.launch(Dispatchers.IO){
//                try{
//                    draftNoteDao.deleteFile(targetNote.draftNoteId!!, file.localFileId!!)
//
//                    loadDraftNotes()
//                }catch(e: Exception){
//                    Log.e("DraftNotesViewModel", "更新に失敗した", e)
//                }
//            }
//        }
//    }

    fun deleteDraftNote(draftNote: DraftNote){
        viewModelScope.launch(Dispatchers.IO){
            try{
                draftNoteDao.deleteDraftNote(draftNote)
            }catch(e: Exception){
                Log.e("DraftNotesViewModel", "下書きノート削除に失敗しました", e)
            }
        }
    }

}



data class DraftNotesPageUiState(
    val visibleContentDraftNoteIds: Set<Long>,
    val draftNotes: ResultState<List<DraftNote>>,
) {
    val draftNoteUiStateList
        get() = draftNotes.convert { list ->
            list.map { note ->
                DraftNoteUiState(note, note.cw == null || visibleContentDraftNoteIds.contains(note.draftNoteId))
            }
        }
}

data class DraftNoteUiState(
    val draftNote: DraftNote,
    val isVisibleContent: Boolean,
)