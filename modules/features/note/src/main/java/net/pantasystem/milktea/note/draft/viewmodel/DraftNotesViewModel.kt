package net.pantasystem.milktea.note.draft.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.notes.draft.DraftNote
import net.pantasystem.milktea.model.notes.draft.DraftNoteFile
import net.pantasystem.milktea.model.notes.draft.DraftNoteRepository
import net.pantasystem.milktea.model.notes.draft.DraftNoteService
import javax.inject.Inject

@HiltViewModel
class DraftNotesViewModel @Inject constructor(
    val accountStore: AccountStore,
    val loggerFactory: Logger.Factory,
    val draftNoteRepository: DraftNoteRepository,
    val driveFileRepository: DriveFileRepository,
    val draftNoteService: DraftNoteService,
) : ViewModel() {


    val logger = loggerFactory.create("DraftNotesVM")

    private val visibleContentDraftNoteIds: StateFlow<Set<Long>> = MutableStateFlow(emptySet())

    @OptIn(ExperimentalCoroutinesApi::class)
    val draftNotesState: StateFlow<ResultState<List<DraftNote>>> = accountStore.state.mapNotNull {
        it.currentAccountId
    }.flatMapLatest { currentAccountId ->
        draftNoteRepository.observeByAccountId(currentAccountId).map {
            @Suppress("USELESS_CAST")
            ResultState.Fixed(StateContent.Exist(it)) as ResultState<List<DraftNote>>
        }
    }.catch { e ->
        this.emit(ResultState.Error(StateContent.NotExist(), e))
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        ResultState.Loading(StateContent.NotExist())
    )

    val uiState = combine(visibleContentDraftNoteIds, draftNotesState) { ids, state ->
        DraftNotesPageUiState(
            visibleContentDraftNoteIds = ids,
            draftNotes = state
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        DraftNotesPageUiState(emptySet(), ResultState.Loading(StateContent.NotExist()))
    )


    fun detachFile(draftNote: DraftNote, file: DraftNoteFile) {
        viewModelScope.launch {
            draftNoteRepository.save(
                draftNote.copy(
                    draftFiles = draftNote.draftFiles?.filterNot {
                        it == file
                    }
                )
            ).onFailure {
                logger.error("detach file error", it)
            }
        }
    }

    fun toggleSensitive(file: DraftNoteFile) {
        viewModelScope.launch {
            when (file) {
                is DraftNoteFile.Remote -> {
                    driveFileRepository.update(
                        file.fileProperty.update(
                            isSensitive = !file.fileProperty.isSensitive
                        )
                    )
                }
                is DraftNoteFile.Local -> {
                    draftNoteService.save(
                        file.copy(isSensitive = file.isSensitive?.not())
                    )
                }
            }.onFailure {
                logger.error("toggle sensitiveに失敗", it)
            }

        }
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

    fun deleteDraftNote(draftNote: DraftNote) {
        viewModelScope.launch {
            draftNoteRepository.delete(draftNote.draftNoteId).onFailure {
                logger.error("下書きノートの削除に失敗しました", it)
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
                DraftNoteUiState(
                    note,
                    note.cw == null || visibleContentDraftNoteIds.contains(note.draftNoteId)
                )
            }
        }
}

data class DraftNoteUiState(
    val draftNote: DraftNote,
    val isVisibleContent: Boolean,
)