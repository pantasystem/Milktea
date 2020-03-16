package jp.panta.misskeyandroidclient.viewmodel.notes.detail

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    val accountRelation: AccountRelation,
    val miCore: MiCore,
    val noteId: String,
    val requestBase: NoteRequest.Setting = NoteRequest.Setting(type = NoteType.DETAIL, noteId = noteId),
    val encryption: Encryption = miCore.getEncryption()
) : ViewModel(){

    private val connectionInformation: EncryptedConnectionInformation = accountRelation.getCurrentConnectionInformation()!!
    private val misskeyAPI: MisskeyAPI = miCore.getMisskeyAPI(connectionInformation)

    val notes = MutableLiveData<List<PlaneNoteViewData>>()


    fun loadDetail(){

        viewModelScope.launch(Dispatchers.IO){
            try{
                val rawDetail = misskeyAPI.showNote(requestBase.buildRequest(connectionInformation, NoteRequest.Conditions(), encryption)).execute().body()
                    ?:return@launch
                val detail = NoteDetailViewData(rawDetail, accountRelation.account)
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
            val children = misskeyAPI.children(NoteRequest(connectionInformation.getI(encryption), limit = 100,noteId =  next.toShowNote.id)).execute().body()?.map{
                PlaneNoteViewData(it,accountRelation.account)
            }
            noteConversationViewData.nextChildren = children
            getChildrenToIterate(noteConversationViewData, conversation)
        }
    }


    private fun loadConversation(): List<PlaneNoteViewData>?{
        return misskeyAPI.conversation(requestBase.buildRequest(connectionInformation, NoteRequest.Conditions(), encryption)).execute().body()?.map{
            PlaneNoteViewData(it, accountRelation.account)
        }
    }

    private fun loadChildren(): List<NoteConversationViewData>?{
        return loadChildren(id = noteId)?.filter{
            it.reNote?.id != noteId
        }?.map{
            val planeNoteViewData = PlaneNoteViewData(it, accountRelation.account)
            val childInChild = loadChildren(planeNoteViewData.toShowNote.id)?.map{n ->
                PlaneNoteViewData(n, accountRelation.account)
            }
            NoteConversationViewData(it, childInChild, accountRelation.account).apply{
                this.hasConversation.postValue(this.getNextNoteForConversation() != null)
            }
        }

    }

    private fun loadChildren(id: String): List<Note>?{
        return misskeyAPI.children(NoteRequest(i = connectionInformation.getI(encryption)!!, limit = 100, noteId = id)).execute().body()
    }


}