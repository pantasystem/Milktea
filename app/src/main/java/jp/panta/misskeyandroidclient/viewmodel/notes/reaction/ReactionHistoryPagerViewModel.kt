package jp.panta.misskeyandroidclient.viewmodel.notes.reaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryRequest
import jp.panta.misskeyandroidclient.streaming.NoteUpdated
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.sql.DataSource

@ExperimentalCoroutinesApi
class ReactionHistoryPagerViewModel(
    val noteId: Note.Id,
    val noteRepository: NoteRepository,
    val adapter: NoteCaptureAPIAdapter,
    val logger: Logger?
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(val noteId: Note.Id, val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReactionHistoryPagerViewModel(noteId = noteId,
                noteRepository = miCore.getNoteRepository(),
                adapter = miCore.getNoteCaptureAdapter(),
                logger = miCore.loggerFactory.create("ReactionHistoryPagerVM")) as T
        }
    }

    private val mNote = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = mNote
    val types: Flow<List<ReactionHistoryRequest>> = note.mapNotNull { note ->
        note?.id?.let {  note.id to note.reactionCounts }
    }.map { idAndList ->
        idAndList.second.map { count ->
            count.reaction
        }.map {
            ReactionHistoryRequest(idAndList.first, it)
        }
    }.shareIn(viewModelScope, SharingStarted.Eagerly)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                noteRepository.find(noteId)
            }.onSuccess {
                mNote.value = it
            }.onFailure {
                logger?.debug("ノート取得エラー noteId: $noteId", e = it)
            }
        }

        adapter.capture(noteId).mapNotNull {
            (it as? NoteDataSource.Event.Updated)?.note?: (it as? NoteDataSource.Event.Created)?.note
        }.onEach {
            mNote.value = it
        }
    }

}