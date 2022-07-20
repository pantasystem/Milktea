package jp.panta.misskeyandroidclient.ui.notes.viewmodel.detail

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.url.UrlPreviewLoadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.gettters.NoteRelationGetter
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.notes.toNoteRequest
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.NoteTranslationStore

@Suppress("BlockingMethodInNonBlockingContext")
class NoteDetailViewModel @AssistedInject constructor(
    private val encryption: Encryption,
    private val noteDataSourceAdder: NoteDataSourceAdder,
    private val accountRepository: AccountRepository,
    private val noteCaptureAdapter: NoteCaptureAPIAdapter,
    private val noteRelationGetter: NoteRelationGetter,
    private val noteRepository: NoteRepository,
    private val noteTranslationStore: NoteTranslationStore,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val miCore: MiCore,
    @Assisted val show: Pageable.Show,
    @Assisted val accountId: Long? = null,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(show: Pageable.Show, accountId: Long?): NoteDetailViewModel
    }

    companion object

    val notes = MutableLiveData<List<PlaneNoteViewData>>()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadDetail() {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val account = getAccount()
                val note = noteRepository.find(Note.Id(account.accountId, show.noteId))
                    .getOrThrow()

                val noteDetail = noteRelationGetter.get(note).getOrThrow()

                val detail = NoteDetailViewData(
                    noteDetail,
                    getAccount(),
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
        val account = getAccount()
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
            val children = misskeyAPIProvider.get(getAccount()).children(
                NoteRequest(
                    getAccount().getI(encryption),
                    limit = 100,
                    noteId = next.toShowNote.note.id.noteId
                )
            ).body()?.map {
                val n = noteDataSourceAdder.addNoteDtoToDataSource(getAccount(), it)
                PlaneNoteViewData(
                    noteRelationGetter.get(n).getOrThrow(),
                    getAccount(),
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
        return misskeyAPIProvider.get(getAccount()).conversation(makeRequest()).body()
            ?.map {
                noteRelationGetter.get(
                    noteDataSourceAdder.addNoteDtoToDataSource(
                        getAccount(),
                        it
                    )
                )
            }?.map {
                PlaneNoteViewData(
                    it.getOrThrow(),
                    getAccount(),
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
                    getAccount(),
                    it
                )
            )
        }?.map {
            val planeNoteViewData = PlaneNoteViewData(
                it.getOrThrow(),
                getAccount(),
                noteCaptureAdapter,
                noteTranslationStore,
            )
            val childInChild = loadChildren(planeNoteViewData.toShowNote.note.id.noteId)?.map { n ->

                PlaneNoteViewData(
                    noteRelationGetter.get(
                        noteDataSourceAdder.addNoteDtoToDataSource(
                            getAccount(),
                            n
                        )
                    ).getOrThrow(),
                    getAccount(),
                    noteCaptureAdapter,
                    noteTranslationStore,
                ).apply {
                    loadUrlPreview(this)
                }
            }
            NoteConversationViewData(
                it.getOrThrow(),
                childInChild,
                getAccount(),
                noteCaptureAdapter,
                noteTranslationStore,
            ).apply {
                this.hasConversation.postValue(this.getNextNoteForConversation() != null)
            }
        }

    }

    private suspend fun loadChildren(id: String): List<NoteDTO>? {
        return misskeyAPIProvider.get(getAccount())
            .children(NoteRequest(i = getAccount().getI(encryption), limit = 100, noteId = id))
            .body()
    }

    private suspend fun loadUrlPreview(planeNoteViewData: PlaneNoteViewData) {
        UrlPreviewLoadTask(
            miCore.getUrlPreviewStore(getAccount()),
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

    private var mAc: Account? = null
    private suspend fun getAccount(): Account {
        if (mAc != null) {
            return mAc!!
        }

        if (accountId != null) {
            mAc = accountRepository.get(accountId).getOrThrow()
            return mAc!!
        }

        mAc = accountRepository.getCurrentAccount().getOrThrow()
        return mAc!!
    }

    private suspend fun makeRequest(): NoteRequest {
        return show.toParams().toNoteRequest(i = getAccount().getI(encryption))
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