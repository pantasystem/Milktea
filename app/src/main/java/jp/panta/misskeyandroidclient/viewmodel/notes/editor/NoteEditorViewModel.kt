package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewData
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

    val driveImages = MediatorLiveData<List<FileProperty>>()

    val localImages = MediatorLiveData<List<File>>()

    val totalImageCount = MediatorLiveData<Int>().apply{
        this.addSource(driveImages){
            value = localImages.value?.size?: 0 + it.size
        }
        this.addSource(localImages){
            value = driveImages.value?.size?:0 + it.size
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

    fun addLocalFile(file: File): Boolean{
        val files = localImages.value
        return when {
            files == null -> {
                localImages.value = listOf(file)
                true
            }
            files.size >= 4 -> false
            else -> {
                localImages.value = ArrayList<File>(files).apply{
                    add(file)
                }
                true
            }
        }
    }

}