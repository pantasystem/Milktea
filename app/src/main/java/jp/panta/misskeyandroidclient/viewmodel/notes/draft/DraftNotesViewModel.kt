package jp.panta.misskeyandroidclient.viewmodel.notes.draft

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.file.FileListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DraftNotesViewModel(
    val draftNoteDao: DraftNoteDao,
    val miCore: MiCore
) : ViewModel(){

    val draftNotes = object : MediatorLiveData<List<DraftNoteViewData>>(){
        override fun onActive() {
            super.onActive()
            if(value.isNullOrEmpty()){
                loadDraftNotes()
            }
        }
    }.apply{
        addSource(miCore.currentAccount){
            loadDraftNotes()
        }
    }

    val isLoading = MutableLiveData<Boolean>()

    fun loadDraftNotes(ar: AccountRelation){
        viewModelScope.launch(Dispatchers.IO){
            try{
                val notes = draftNoteDao.findDraftNotesByAccount(ar.account.id)
                draftNotes.postValue(notes.map{
                    DraftNoteViewData(it)
                })
            }catch(e: Exception){

            }finally {
                isLoading.value = false
            }
        }
    }

    fun loadDraftNotes(){
        miCore.currentAccount.value?.let{
            loadDraftNotes(it)
        }
    }

    fun detachFile(file: File?) {
        file?.localFileId?.let{
            val notes = ArrayList(draftNotes.value?: emptyList())
            val targetNote = (notes.firstOrNull {
                it.note.value?.files?.any{ f ->
                    f == file
                }?: false
            }?: return).note.value ?: return

            val updatedFiles = ArrayList(targetNote.files?: emptyList())
            updatedFiles.remove(file)

            val updatedDraftNote = targetNote.copy(files = updatedFiles)
            viewModelScope.launch(Dispatchers.IO){
                try{
                    draftNoteDao.fullInsert(updatedDraftNote)

                    loadDraftNotes()
                }catch(e: Exception){
                    Log.e("DraftNotesViewModel", "更新に失敗した", e)
                }
            }
        }
    }

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