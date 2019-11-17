package jp.panta.misskeyandroidclient.viewmodel.notes.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    val connectionInstance: ConnectionInstance,
    val misskeyAPI: MisskeyAPI,
    val note: PlaneNoteViewData,
    val requestBase: NoteRequest.Setting = NoteRequest.Setting(type = NoteType.DETAIL, noteId = note.toShowNote.id)
) : ViewModel(){

    val notes = MutableLiveData<List<PlaneNoteViewData>>()

    fun loadDetail(){

        viewModelScope.launch(Dispatchers.IO){
            try{
                val rawDetail = misskeyAPI.showNote(requestBase.buildRequest(connectionInstance, NoteRequest.Conditions())).execute().body()
                    ?:return@launch
                val detail = NoteDetailViewData(rawDetail)
                var list: List<PlaneNoteViewData> = listOf(detail)
                notes.postValue(list)

                val conversation = loadConversation()
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

            }catch (e: Exception){

            }
        }

    }


    fun loadNewConversation(noteConversationViewData: NoteConversationViewData){
        viewModelScope.launch(Dispatchers.IO){
            try{
                val conversation = noteConversationViewData.conversation.value
                    ?: emptyList()
                getChildrenToIterate(noteConversationViewData, ArrayList(conversation))
            }catch(e: Exception){

            }
        }
    }

    private fun getChildrenToIterate(
        noteConversationViewData: NoteConversationViewData,
        conversation: ArrayList<PlaneNoteViewData>
    ): NoteConversationViewData{
        val next = noteConversationViewData.getNextNoteForConversation()
        return if(next == null){
            noteConversationViewData.conversation.postValue(conversation)
            noteConversationViewData.hasConversation.postValue(false)
            noteConversationViewData
        }else{
            conversation.add(next)
            val children = misskeyAPI.children(NoteRequest(connectionInstance.getI(), limit = 100,noteId =  next.toShowNote.id)).execute().body()?.map{
                PlaneNoteViewData(it)
            }
            noteConversationViewData.nextChildren = children
            getChildrenToIterate(noteConversationViewData, conversation)
        }
    }


    private fun loadConversation(): List<PlaneNoteViewData>?{
        return misskeyAPI.conversation(requestBase.buildRequest(connectionInstance, NoteRequest.Conditions())).execute().body()?.map{
            PlaneNoteViewData(it)
        }
    }

    private fun loadChildren(): List<NoteConversationViewData>?{
        return loadChildren(id = note.toShowNote.id)?.filter{
            it.reNote?.id != note.toShowNote.id
        }?.map{
            val planeNoteViewData = PlaneNoteViewData(it)
            val childInChild = loadChildren(planeNoteViewData.toShowNote.id)?.map{n ->
                PlaneNoteViewData(n)
            }
            NoteConversationViewData(it, childInChild).apply{
                this.hasConversation.postValue(this.getNextNoteForConversation() != null)
            }
        }

    }

    private fun loadChildren(id: String): List<Note>?{
        return misskeyAPI.children(NoteRequest(i = connectionInstance.getI()!!, limit = 100, noteId = id)).execute().body()
    }


}