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
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLengthSettingStore
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.url.UrlPreviewLoadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class NoteDetailViewModel(
    val account: Account,
    val show: Pageable.Show,
    val miCore: MiCore,
    val encryption: Encryption = miCore.getEncryption()
) : ViewModel(){

    private val request = NoteRequest(
        i = account.getI(encryption),
        noteId = show.noteId
    )

    private val misskeyAPI: MisskeyAPI = miCore.getMisskeyAPI(account)
    //private val streamingAdapter = StreamingAdapter(connectionInformation, encryption)


    private val noteCapture = miCore.getNoteCapture(account)
    private val client = jp.panta.misskeyandroidclient.model.streming.note.v2.NoteCapture.Client()
    private val noteEventStore = miCore.getNoteEventStore(account)


    val notes = object : MutableLiveData<List<PlaneNoteViewData>>(){
        override fun onActive() {
            super.onActive()

            startStreaming()
        }

        override fun onInactive() {
            super.onInactive()

            stopStreaming()
        }
    }

    //private val mNoteDetailId = UUID.randomUUID().toString()

    private var mNoteCaptureDisposable: Disposable? = null

    private fun startStreaming(){

        //noteCapture.attach(noteRegister)
        noteCapture.attachClient(client)
        if(mNoteCaptureDisposable == null){
            mNoteCaptureDisposable = noteEventStore.getEventStream(Date()).subscribe {
                noteEventObserver(it)
            }
        }

    }

    private fun stopStreaming(){
        //noteCapture.detach(noteRegister)
        noteCapture.detachClient(client)
        mNoteCaptureDisposable?.dispose()
        mNoteCaptureDisposable = null
    }


    fun loadDetail(){

        viewModelScope.launch(Dispatchers.IO){
            try{
                val rawDetail = misskeyAPI.showNote(request).execute().body()
                    ?:return@launch
                val detail = NoteDetailViewData(rawDetail, account, DetermineTextLengthSettingStore(miCore.getSettingStore()))
                loadUrlPreview(detail)
                var list: List<PlaneNoteViewData> = listOf(detail)
                notes.postValue(list)

                val conversation = loadConversation()?.asReversed()
                if(conversation != null){
                    list = ArrayList<PlaneNoteViewData>(conversation).apply{
                        addAll(list)
                    }
                    notes.postValue(list)
                }
                val children = loadChildren()
                if(children != null){
                    list = ArrayList<PlaneNoteViewData>(list).apply{
                        addAll(children)
                    }
                    notes.postValue(list)
                }
                //noteCapture.subscribeAll(noteRegister.registerId, list)

                val noteIds = HashSet<String>()

                for(note in list){

                    noteIds.add(note.id)
                    noteIds.add(note.toShowNote.id)
                }
                client.captureAll(noteIds.toList())

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

    private fun getChildrenToIterate(
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
            val children = misskeyAPI.children(NoteRequest(account.getI(encryption), limit = 100,noteId =  next.toShowNote.id)).execute().body()?.map{
                PlaneNoteViewData(it, account, DetermineTextLengthSettingStore(miCore.getSettingStore())).apply{
                    loadUrlPreview(this)
                }
            }
            noteConversationViewData.nextChildren = children
            getChildrenToIterate(noteConversationViewData, conversation)
        }
    }


    private fun loadConversation(): List<PlaneNoteViewData>?{
        return misskeyAPI.conversation(request).execute().body()?.map{
            PlaneNoteViewData(it, account, DetermineTextLengthSettingStore(miCore.getSettingStore())).apply{
                loadUrlPreview(this)
            }
        }
    }

    private fun loadChildren(): List<NoteConversationViewData>?{
        return loadChildren(id = show.noteId)?.filter{
            it.reNote?.id != show.noteId
        }?.map{
            val planeNoteViewData = PlaneNoteViewData(it, account, DetermineTextLengthSettingStore(miCore.getSettingStore()))
            val childInChild = loadChildren(planeNoteViewData.toShowNote.id)?.map{n ->
                PlaneNoteViewData(n, account, DetermineTextLengthSettingStore(miCore.getSettingStore())).apply{
                    loadUrlPreview(this)
                }
            }
            NoteConversationViewData(it, childInChild, account, DetermineTextLengthSettingStore(miCore.getSettingStore())).apply{
                this.hasConversation.postValue(this.getNextNoteForConversation() != null)
            }
        }

    }

    private fun loadChildren(id: String): List<NoteDTO>?{
        return misskeyAPI.children(NoteRequest(i = account.getI(encryption), limit = 100, noteId = id)).execute().body()
    }

    private fun loadUrlPreview(planeNoteViewData: PlaneNoteViewData){
        UrlPreviewLoadTask(
            miCore.getUrlPreviewStore(account),
            planeNoteViewData.urls,
            viewModelScope
        ).load(planeNoteViewData.urlPreviewLoadTaskCallback)
    }

    private fun noteEventObserver(noteEvent: NoteCaptureEvent){
        Log.d("TM-VM", "#noteEventObserver $noteEvent")
        val timelineNotes = notes.value
            ?: return

        val updatedNotes = when(noteEvent.event){

            is Event.Deleted ->{
                timelineNotes.filterNot{ note ->
                    note.id == noteEvent.noteId || note.toShowNote.id == noteEvent.noteId
                }
            }
            else -> timelineNotes.map{
                val note: PlaneNoteViewData = it
                if(note.toShowNote.id == noteEvent.noteId){
                    when(noteEvent.event){
                        is Event.NewNote.Reacted ->{
                            it.addReaction(noteEvent.event.reaction, noteEvent.event.emoji, noteEvent.event.userId == account.remoteId)
                        }
                        is Event.NewNote.UnReacted ->{
                            it.takeReaction(noteEvent.event.reaction, noteEvent.event.userId == account.remoteId)
                        }
                        is Event.NewNote.Voted ->{
                            it.poll?.update(noteEvent.event.choice, noteEvent.event.userId == account.remoteId)
                        }
                        /*is Event.Added ->{
                            note.update(noteEvent.event.note)
                        }*/
                    }
                }

                note
            }
        }
        if(noteEvent.event is Event.Deleted){
            notes.postValue(updatedNotes)
        }
    }

}