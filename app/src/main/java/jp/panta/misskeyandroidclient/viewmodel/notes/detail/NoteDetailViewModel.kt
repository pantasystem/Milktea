package jp.panta.misskeyandroidclient.viewmodel.notes.detail

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.Encryption
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
    val noteId: String,
    val requestBase: NoteRequest.Setting = NoteRequest.Setting(type = NoteType.DETAIL, noteId = noteId),
    val encryption: Encryption
) : ViewModel(){

    val notes = MutableLiveData<List<PlaneNoteViewData>>()

    fun loadDetail(){

        viewModelScope.launch(Dispatchers.IO){
            try{
                val rawDetail = misskeyAPI.showNote(requestBase.buildRequest(connectionInstance, NoteRequest.Conditions(), encryption)).execute().body()
                    ?:return@launch
                val detail = NoteDetailViewData(rawDetail, connectionInstance)
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
            val children = misskeyAPI.children(NoteRequest(connectionInstance.getI(encryption), limit = 100,noteId =  next.toShowNote.id)).execute().body()?.map{
                PlaneNoteViewData(it,connectionInstance)
            }
            noteConversationViewData.nextChildren = children
            getChildrenToIterate(noteConversationViewData, conversation)
        }
    }


    private fun loadConversation(): List<PlaneNoteViewData>?{
        return misskeyAPI.conversation(requestBase.buildRequest(connectionInstance, NoteRequest.Conditions(), encryption)).execute().body()?.map{
            PlaneNoteViewData(it, connectionInstance)
        }
    }

    private fun loadChildren(): List<NoteConversationViewData>?{
        return loadChildren(id = noteId)?.filter{
            it.reNote?.id != noteId
        }?.map{
            val planeNoteViewData = PlaneNoteViewData(it, connectionInstance)
            val childInChild = loadChildren(planeNoteViewData.toShowNote.id)?.map{n ->
                PlaneNoteViewData(n, connectionInstance)
            }
            NoteConversationViewData(it, childInChild, connectionInstance).apply{
                this.hasConversation.postValue(this.getNextNoteForConversation() != null)
            }
        }

    }

    private fun loadChildren(id: String): List<Note>?{
        return misskeyAPI.children(NoteRequest(i = connectionInstance.getI(encryption)!!, limit = 100, noteId = id)).execute().body()
    }


}