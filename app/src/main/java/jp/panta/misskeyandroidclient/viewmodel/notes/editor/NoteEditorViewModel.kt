package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.OkHttpDriveFileUploader
import jp.panta.misskeyandroidclient.model.drive.UploadFile
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.view.notes.editor.FileNoteEditorData
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.poll.PollEditor
import java.io.File

class NoteEditorViewModel(
    private val connectionInstance: ConnectionInstance,
    private val misskeyAPI: MisskeyAPI,
    meta: Meta,
    private val replyToNoteId: String? = null,
    private val quoteToNoteId: String? = null
) : ViewModel(){
    val hasCw = MutableLiveData<Boolean>(false)
    val cw = MutableLiveData<String>()
    val text = MutableLiveData<String>("")
    val maxTextLength = meta.maxNoteTextLength?: 1500
    val textRemaining = Transformations.map(text){
        maxTextLength - it.length
    }

    val editorFiles = MediatorLiveData<List<FileNoteEditorData>>()

    val totalImageCount = MediatorLiveData<Int>().apply{

        this.addSource(editorFiles){
            Log.d("NoteEditorViewModel", "list$it, sizeは: ${it.size}")
            this.value = it.size
        }
    }


    val isPostAvailable = MediatorLiveData<Boolean>().apply{
        this.addSource(textRemaining){
            val totalImageTmp = totalImageCount.value
            this.value =  it in 0 until maxTextLength
                    || (totalImageTmp != null && totalImageTmp > 0 && totalImageTmp <= 4)
                    || quoteToNoteId != null
        }
        this.addSource(totalImageCount){
            val tmpTextSize = textRemaining.value
            this.value = tmpTextSize in 0 until maxTextLength
                    || (it != null && it > 0 && it <= 4)
                    || quoteToNoteId != null
        }
    }

    val visibility = MutableLiveData<String>()

    val poll = MutableLiveData<PollEditor?>()

    val noteTask = MutableLiveData<PostNoteTask>()

    fun post(){
        val noteTask = PostNoteTask(connectionInstance)
        noteTask.cw = cw.value
        noteTask.files = editorFiles.value
        //noteTask.localOnl
        //noteTask.noExtractEmojis =
        noteTask.text =text.value
        noteTask.poll = poll.value?.buildCreatePoll()
        noteTask.renoteId = quoteToNoteId
        noteTask.replyId = replyToNoteId
        //noteTask.setVisibility()
        this.noteTask.postValue(noteTask)
    }

    fun add(file: Uri){
        val files = editorFiles.value.toArrayList()
        files.add(FileNoteEditorData(UploadFile(file, true)))
        editorFiles.value = files
    }

    fun add(fp: FileProperty){
        val files = editorFiles.value.toArrayList()
        files.add(FileNoteEditorData(fp))
        editorFiles.value = files
    }

    fun addAllFile(file: List<Uri>){
        val files = editorFiles.value.toArrayList()
        files.addAll(file.map{
            FileNoteEditorData(UploadFile(it, true))
        })
        editorFiles.value = files
    }

    fun addAllFileProperty(fpList: List<FileProperty>){
        val files = editorFiles.value.toArrayList()
        files.addAll(fpList.map{
            FileNoteEditorData(it)
        })
        editorFiles.value = files
    }

    fun removeFileNoteEditorData(data: FileNoteEditorData){
        val files = editorFiles.value.toArrayList()
        files.remove(data)
        editorFiles.value = files
    }

    fun localFileTotal(): Int{
        return editorFiles.value?.filter{
            it.isLocal
        }?.size?: 0
    }

    fun driveFileTotal(): Int{
        return editorFiles.value?.filter{
            !it.isLocal
        }?.size?: 0
    }

    fun fileTotal(): Int{
        return editorFiles.value?.size?: 0
    }

    fun driveFiles(): List<FileProperty>{
        return editorFiles.value?.filter {
            !it.isLocal && it.fileProperty != null
        }?.mapNotNull {
            it.fileProperty
        } ?: emptyList()
    }



    fun changeCwEnabled(){
        hasCw.value = !(hasCw.value?: false)
    }

    fun enablePoll(){
        val p = poll.value
        if(p == null){
            poll.value = PollEditor()
        }
    }

    fun disablePoll(){
        val p = poll.value
        if(p != null){
            poll.value = null
        }
    }


    private fun List<FileNoteEditorData>?.toArrayList(): ArrayList<FileNoteEditorData>{
        return if(this == null){
            ArrayList<FileNoteEditorData>()
        }else{
            ArrayList<FileNoteEditorData>(this)
        }
    }


}