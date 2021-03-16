package jp.panta.misskeyandroidclient.viewmodel.notes.detail

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.Disposable
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.notes.Event
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureEvent
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.api.notes.toEntities
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLengthSettingStore
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.url.UrlPreviewLoadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

@Suppress("BlockingMethodInNonBlockingContext")
class NoteDetailViewModel(
    val show: Pageable.Show,
    val miCore: MiCore,
    val accountId: Long? = null,
    val encryption: Encryption = miCore.getEncryption()
) : ViewModel(){

    /*private val request = NoteRequest(
        i = account.getI(encryption),
        noteId = show.noteId
    )*/

    //private val misskeyAPI: MisskeyAPI = miCore.getMisskeyAPI(account)

    val notes = MutableLiveData<List<PlaneNoteViewData>>()



    fun loadDetail(){

        viewModelScope.launch(Dispatchers.IO){
            try{
                val rawDetail = miCore.getMisskeyAPI(getAccount()).showNote(makeRequest()).execute().body()
                    ?:return@launch

                val noteDetail = miCore.getGetters().noteRelationGetter.get(getAccount(), rawDetail)

                val detail = NoteDetailViewData(
                    noteDetail,
                    getAccount(),
                    DetermineTextLengthSettingStore(miCore.getSettingStore()),
                    miCore.getNoteCaptureAdapter()
                )
                loadUrlPreview(detail)
                var list: List<PlaneNoteViewData> = listOf(detail)
                notes.postValue(list)

                val conversation = loadConversation()?.asReversed()
                if(conversation != null){
                    list = ArrayList<PlaneNoteViewData>(conversation).apply{
                        addAll(list)
                    }
                    list.captureAll()
                    notes.postValue(list)
                }
                val children = loadChildren()
                if(children != null){
                    list = ArrayList<PlaneNoteViewData>(list).apply{
                        addAll(children)
                    }
                    list.captureAll()
                    notes.postValue(list)
                }

            }catch (e: Exception){

            }
        }

    }


    fun loadNewConversation(noteConversationViewData: NoteConversationViewData){
        Log.d("NoteDetailViewModel", "新たにConversationを読み込もうとした")
        viewModelScope.launch(Dispatchers.IO){
            try{
                val conversation = noteConversationViewData.conversation.value
                    ?: emptyList()
                getChildrenToIterate(noteConversationViewData, ArrayList(conversation))
            }catch(e: Exception){
                Log.e("NoteDetailViewModel", "loadNewConversation中にエラー発生", e)
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun getChildrenToIterate(
        noteConversationViewData: NoteConversationViewData,
        conversation: ArrayList<PlaneNoteViewData>
    ): NoteConversationViewData{
        val next = noteConversationViewData.getNextNoteForConversation()
        println("ねくすと: ${next?.id}, :${next?.text}")
        return if(next == null){
            noteConversationViewData.conversation.postValue(conversation)
            noteConversationViewData.hasConversation.postValue(false)
            println("こんばーせーしょんの最終さいずは:${conversation.size}")
            noteConversationViewData
        }else{
            conversation.add(next)
            val children = miCore.getMisskeyAPI(getAccount()).children(NoteRequest(getAccount().getI(encryption), limit = 100,noteId =  next.toShowNote.note.id.noteId)).execute().body()?.map{
                PlaneNoteViewData(
                    miCore.getGetters().noteRelationGetter.get(getAccount(), it),
                    getAccount(),
                    DetermineTextLengthSettingStore(miCore.getSettingStore()),
                    miCore.getNoteCaptureAdapter()
                ).apply{
                    loadUrlPreview(this)
                }
            }
            noteConversationViewData.nextChildren = children
            getChildrenToIterate(noteConversationViewData, conversation)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun loadConversation(): List<PlaneNoteViewData>?{
        return miCore.getMisskeyAPI(getAccount()).conversation(makeRequest()).execute().body()?.map{
            miCore.getGetters().noteRelationGetter.get(getAccount(), it)
        }?.map{
            PlaneNoteViewData(
                it,
                getAccount(),
                DetermineTextLengthSettingStore(miCore.getSettingStore()),
                miCore.getNoteCaptureAdapter()
            ).apply{
                loadUrlPreview(this)
            }
        }
    }

    private suspend fun loadChildren(): List<NoteConversationViewData>?{
        return loadChildren(id = show.noteId)?.filter{
            it.reNote?.id != show.noteId
        }?.map{
            miCore.getGetters().noteRelationGetter.get(getAccount(), it)
        }?.map{
            val planeNoteViewData = PlaneNoteViewData(it, getAccount(), DetermineTextLengthSettingStore(miCore.getSettingStore()), miCore.getNoteCaptureAdapter())
            val childInChild = loadChildren(planeNoteViewData.toShowNote.note.id.noteId)?.map{n ->
                PlaneNoteViewData(miCore.getGetters().noteRelationGetter.get(getAccount(), n), getAccount(), DetermineTextLengthSettingStore(miCore.getSettingStore()), miCore.getNoteCaptureAdapter()).apply{
                    loadUrlPreview(this)
                }
            }
            NoteConversationViewData(
                it,
                childInChild,
                getAccount(),
                DetermineTextLengthSettingStore(miCore.getSettingStore()),
                miCore.getNoteCaptureAdapter()
            ).apply{
                this.hasConversation.postValue(this.getNextNoteForConversation() != null)
            }
        }

    }

    private suspend fun loadChildren(id: String): List<NoteDTO>?{
        return miCore.getMisskeyAPI(getAccount()).children(NoteRequest(i = getAccount().getI(encryption), limit = 100, noteId = id)).execute().body()
    }

    private suspend fun loadUrlPreview(planeNoteViewData: PlaneNoteViewData){
        UrlPreviewLoadTask(
            miCore.getUrlPreviewStore(getAccount()),
            planeNoteViewData.urls,
            viewModelScope
        ).load(planeNoteViewData.urlPreviewLoadTaskCallback)
    }

    private fun<T: PlaneNoteViewData> T.capture():  T{
        val self = this
        viewModelScope.launch(Dispatchers.IO) {
            self.eventFlow.collect()
        }
        return this
    }

    private fun<T: PlaneNoteViewData> List<T>.captureAll() {
        this.forEach {
            it.capture()
        }
    }

    private var mAc: Account? = null
    private suspend fun getAccount(): Account {
        if(mAc != null) {
            return mAc!!
        }

        if(accountId != null) {
            mAc = miCore.getAccountRepository().get(accountId)
            return mAc!!
        }

        mAc = miCore.getAccountRepository().getCurrentAccount()
        return mAc!!
    }

    private suspend fun makeRequest(): NoteRequest {
        return show.toParams().toNoteRequest(i = getAccount().getI(miCore.getEncryption()))
    }
}