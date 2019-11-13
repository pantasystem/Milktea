package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.view.notes.editor.FileNoteEditorData
import java.io.File

class NoteEditorViewModel(
    private val connectionInstance: ConnectionInstance,
    private val misskeyAPI: MisskeyAPI,
    private val meta: Meta
) : ViewModel(){

    val text = MutableLiveData<String>("")
    val maxTextLength = meta.maxNoteTextLength?: 1500
    val textRemaining = Transformations.map(text){
        maxTextLength - it.length
    }

    /*val driveFiles = MediatorLiveData<List<FileProperty>>()

    val localFiles = MediatorLiveData<List<File>>()*/

    val editorFiles = MediatorLiveData<List<FileNoteEditorData>>().apply{
        /*this.addSource(driveFiles){
            val drives = it.map{fp ->
                FileNoteEditorData(fp)
            }
            ArrayList<FileNoteEditorData>(drives).apply{
                val local = localFiles.value?.map{f ->
                    FileNoteEditorData(f)
                }
                if(local != null){
                    addAll(local)
                }
            }
        }
        this.addSource(localFiles){
            val drives = driveFiles.value?.map{
                FileNoteEditorData(it)
            }
        }*/
    }

    val totalImageCount = MediatorLiveData<Int>().apply{
        /*this.addSource(driveFiles){
            val localImageSize = localFiles.value?.size
            val total = if(localImageSize != null){
                localImageSize + it.size
            }else{
                it.size
            }
            this.value = total
        }
        this.addSource(localFiles){
            val driveImageSize = driveFiles.value?.size
            val total = if(driveImageSize != null){
                driveImageSize + it.size
            }else{
                it.size
            }
            value = total
        }*/
        this.addSource(editorFiles){
            Log.d("NoteEditorViewModel", "list$it, sizeは: ${it.size}")
            this.value = it.size
        }
    }


    val isPostAvailable = MediatorLiveData<Boolean>().apply{
        this.addSource(textRemaining){
            val totalImageTmp = totalImageCount.value
            this.value =  it in 0 until maxTextLength || (totalImageTmp != null && totalImageTmp > 0 && totalImageTmp <= 4)
        }
        this.addSource(totalImageCount){
            val tmpTextSize = textRemaining.value
            this.value = tmpTextSize in 0 until maxTextLength || (it != null && it > 0 && it <= 4)
        }
    }

    /*fun addLocalFile(file: File): Boolean{
        val files = localFiles.value
        val totalSize = totalImageCount.value?:0
        return when {
            files == null -> {
                localFiles.value = listOf(file)
                true
            }
            totalSize >= 4 -> false
            else -> {
                localFiles.value = ArrayList<File>(files).apply{
                    add(file)
                }
                true
            }
        }
    }*/

    fun add(file: File){
        val files = editorFiles.value.toArrayList()
        files.add(FileNoteEditorData(file))
        editorFiles.value = files
    }

    fun add(fp: FileProperty){
        val files = editorFiles.value.toArrayList()
        files.add(FileNoteEditorData(fp))
        editorFiles.value = files
    }

    fun addAllFile(file: List<File>){
        val files = editorFiles.value.toArrayList()
        files.addAll(file.map{
            FileNoteEditorData(it)
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

    fun localFiles(): List<File>{
        return editorFiles.value?.filter{
            it.isLocal && it.file != null
        }?.mapNotNull {
            it.file
        }?: emptyList()
    }



    private fun List<FileNoteEditorData>?.toArrayList(): ArrayList<FileNoteEditorData>{
        return if(this == null){
            ArrayList<FileNoteEditorData>()
        }else{
            ArrayList<FileNoteEditorData>(this)
        }
    }


}