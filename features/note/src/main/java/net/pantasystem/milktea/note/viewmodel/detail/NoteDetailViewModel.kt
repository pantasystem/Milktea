package net.pantasystem.milktea.note.viewmodel.detail

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.gettters.NoteRelationGetter
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.notes.toNoteRequest
import net.pantasystem.milktea.data.infrastructure.url.UrlPreviewStoreProvider
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.url.UrlPreviewLoadTask
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

@Suppress("BlockingMethodInNonBlockingContext")
class NoteDetailViewModel @AssistedInject constructor(
    private val encryption: Encryption,
    private val noteDataSourceAdder: NoteDataSourceAdder,
    accountRepository: AccountRepository,
    private val noteCaptureAdapter: NoteCaptureAPIAdapter,
    private val noteRelationGetter: NoteRelationGetter,
    private val noteRepository: NoteRepository,
    private val noteTranslationStore: NoteTranslationStore,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val urlPreviewStoreProvider: UrlPreviewStoreProvider,
    @Assisted val show: Pageable.Show,
    @Assisted val accountId: Long? = null,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(show: Pageable.Show, accountId: Long?): NoteDetailViewModel
    }

    companion object;

    private val currentAccountWatcher: CurrentAccountWatcher = CurrentAccountWatcher(accountId, accountRepository)

    val notes = MutableLiveData<List<PlaneNoteViewData>>()

    fun loadDetail() {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val account = currentAccountWatcher.getAccount()
                val note = noteRepository.find(Note.Id(account.accountId, show.noteId))
                    .getOrThrow()

                val noteDetail = noteRelationGetter.get(note).getOrThrow()

                val detail = NoteDetailViewData(
                    noteDetail,
                    currentAccountWatcher.getAccount(),
                    noteCaptureAdapter,
                    noteTranslationStore,
                )
                loadUrlPreview(detail)
                var list: List<PlaneNoteViewData> = listOf(detail)
                notes.postValue(list)

                val conversation = loadConversation()?.asReversed()
                if (conversation != null) {
                    list = ArrayList<PlaneNoteViewData>(conversation).apply {
                        addAll(list)
                    }
                    list.captureAll()
                    notes.postValue(list)
                }
                val children = loadChildren()
                if (children != null) {
                    list = ArrayList<PlaneNoteViewData>(list).apply {
                        addAll(children)
                    }
                    list.captureAll()
                    notes.postValue(list)
                }

            } catch (e: Exception) {
                Log.w("NoteDetailViewModel", "loadDetail失敗", e)
            }
        }

    }


    fun loadNewConversation(noteConversationViewData: NoteConversationViewData) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val conversation = noteConversationViewData.conversation.value
                    ?: emptyList()
                getChildrenToIterate(noteConversationViewData, ArrayList(conversation))
            } catch (e: Exception) {
                Log.e("NoteDetailViewModel", "loadNewConversation中にエラー発生", e)
            }
        }
    }

    suspend fun getUrl(): String {
        val account = currentAccountWatcher.getAccount()
        return "${account.instanceDomain}/notes/${show.noteId}"
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun getChildrenToIterate(
        noteConversationViewData: NoteConversationViewData,
        conversation: ArrayList<PlaneNoteViewData>
    ): NoteConversationViewData {
        val next = noteConversationViewData.getNextNoteForConversation()
        return if (next == null) {
            noteConversationViewData.conversation.postValue(conversation)
            noteConversationViewData.hasConversation.postValue(false)
            noteConversationViewData
        } else {
            conversation.add(next)
            val children = misskeyAPIProvider.get(currentAccountWatcher.getAccount()).children(
                NoteRequest(
                    currentAccountWatcher.getAccount().getI(encryption),
                    limit = 100,
                    noteId = next.toShowNote.note.id.noteId
                )
            ).body()?.map {
                val n = noteDataSourceAdder.addNoteDtoToDataSource(currentAccountWatcher.getAccount(), it)
                PlaneNoteViewData(
                    noteRelationGetter.get(n).getOrThrow(),
                    currentAccountWatcher.getAccount(),
                    noteCaptureAdapter,
                    noteTranslationStore
                ).apply {
                    loadUrlPreview(this)
                }
            }
            noteConversationViewData.nextChildren = children
            getChildrenToIterate(noteConversationViewData, conversation)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun loadConversation(): List<PlaneNoteViewData>? {
        return misskeyAPIProvider.get(currentAccountWatcher.getAccount()).conversation(makeRequest()).body()
            ?.map {
                noteRelationGetter.get(
                    noteDataSourceAdder.addNoteDtoToDataSource(
                        currentAccountWatcher.getAccount(),
                        it
                    )
                )
            }?.map {
                PlaneNoteViewData(
                    it.getOrThrow(),
                    currentAccountWatcher.getAccount(),
                    noteCaptureAdapter,
                    noteTranslationStore,
                ).apply {
                    loadUrlPreview(this)
                }
            }
    }

    private suspend fun loadChildren(): List<NoteConversationViewData>? {
        return loadChildren(id = show.noteId)?.filter {
            it.reNote?.id != show.noteId
        }?.map {
            noteRelationGetter.get(
                noteDataSourceAdder.addNoteDtoToDataSource(
                    currentAccountWatcher.getAccount(),
                    it
                )
            )
        }?.map {
            val planeNoteViewData = PlaneNoteViewData(
                it.getOrThrow(),
                currentAccountWatcher.getAccount(),
                noteCaptureAdapter,
                noteTranslationStore,
            )
            val childInChild = loadChildren(planeNoteViewData.toShowNote.note.id.noteId)?.map { n ->

                PlaneNoteViewData(
                    noteRelationGetter.get(
                        noteDataSourceAdder.addNoteDtoToDataSource(
                            currentAccountWatcher.getAccount(),
                            n
                        )
                    ).getOrThrow(),
                    currentAccountWatcher.getAccount(),
                    noteCaptureAdapter,
                    noteTranslationStore,
                ).apply {
                    loadUrlPreview(this)
                }
            }
            NoteConversationViewData(
                it.getOrThrow(),
                childInChild,
                currentAccountWatcher.getAccount(),
                noteCaptureAdapter,
                noteTranslationStore,
            ).apply {
                this.hasConversation.postValue(this.getNextNoteForConversation() != null)
            }
        }

    }

    private suspend fun loadChildren(id: String): List<NoteDTO>? {
        return misskeyAPIProvider.get(currentAccountWatcher.getAccount())
            .children(NoteRequest(i = currentAccountWatcher.getAccount().getI(encryption), limit = 100, noteId = id))
            .body()
    }

    private suspend fun loadUrlPreview(planeNoteViewData: PlaneNoteViewData) {
        UrlPreviewLoadTask(
            urlPreviewStoreProvider.getUrlPreviewStore(currentAccountWatcher.getAccount()),
            planeNoteViewData.urls,
            viewModelScope
        ).load(planeNoteViewData.urlPreviewLoadTaskCallback)
    }

    private fun <T : PlaneNoteViewData> T.capture(): T {
        val self = this
        viewModelScope.launch(Dispatchers.IO) {
            self.eventFlow.collect()
        }
        return this
    }

    private fun <T : PlaneNoteViewData> List<T>.captureAll() {
        this.forEach {
            it.capture()
        }
    }


    private suspend fun makeRequest(): NoteRequest {
        return show.toParams().toNoteRequest(i = currentAccountWatcher.getAccount().getI(encryption))
    }
}

@Suppress("UNCHECKED_CAST")
fun NoteDetailViewModel.Companion.provideFactory(
    factory: NoteDetailViewModel.ViewModelAssistedFactory,
    show: Pageable.Show,
    accountId: Long? = null,
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(show, accountId) as T
    }
}