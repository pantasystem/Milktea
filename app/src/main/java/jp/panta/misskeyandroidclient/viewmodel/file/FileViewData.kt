package jp.panta.misskeyandroidclient.viewmodel.file

import androidx.lifecycle.MutableLiveData
import net.pantasystem.milktea.data.model.file.File

class FileViewData(val file: File) {
    enum class Type{
        VIDEO, IMAGE, SOUND, OTHER
    }
    val type = when{
        file.type == null -> Type.OTHER
        file.type!!.startsWith("image") -> Type.IMAGE
        file.type!!.startsWith("video") -> Type.VIDEO
        file.type!!.startsWith("audio") -> Type.SOUND
        else -> Type.OTHER
    }

    val isHiding = MutableLiveData(file.isSensitive ?: false)

    val isImage = type == Type.IMAGE


    fun show(){
        val now = isHiding.value?: false
        if(now){
            isHiding.value = false
        }
    }

    fun toggleVisibility() {
        isHiding.value = !(isHiding.value ?: false)
    }
}