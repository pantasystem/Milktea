package jp.panta.misskeyandroidclient.viewmodel.file

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.file.File

class FileViewData(val file: File) {
    enum class Type{
        VIDEO, IMAGE, SOUND, OTHER
    }
    val name = file.name
    val type = when{
        file.type == null -> Type.OTHER
        file.type.startsWith("image") -> Type.IMAGE
        file.type.startsWith("video") -> Type.VIDEO
        file.type.startsWith("audio") -> Type.SOUND
        else -> Type.OTHER
    }
    val folderId = file.folderId
    val isSensitive = file.isSensitive?: false
    val url = file.path
    val thumbnailUrl = file.thumbnailUrl?: url

    val isHiding = MutableLiveData<Boolean>(isSensitive)

    val isImage = type == Type.IMAGE

    fun changeContentHiding(){
        if(isSensitive){
            val now = isHiding.value?: false
            isHiding.value = !now
        }
    }

    fun show(){
        val now = isHiding.value?: false
        if(isSensitive && now){
            isHiding.value = false
        }
    }

}