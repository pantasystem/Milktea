package jp.panta.misskeyandroidclient.viewmodel.notes.media

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.drive.FileProperty

class FileViewData(val fileProperty: FileProperty) {
    enum class Type{
        VIDEO, IMAGE, SOUND, OTHER
    }
    val id = fileProperty.id
    val name = fileProperty.name
    val type = when{
        fileProperty.type == null -> Type.OTHER
        fileProperty.type.startsWith("image") -> Type.IMAGE
        fileProperty.type.startsWith("video") -> Type.VIDEO
        fileProperty.type.startsWith("audio") -> Type.SOUND
        else -> Type.OTHER
    }
    val md5 = fileProperty.md5
    val size = fileProperty.size
    val userId = fileProperty.userId
    val folderId = fileProperty.folderId
    val comment = fileProperty.comment
    val isSensitive = fileProperty.isSensitive?: false
    val url = fileProperty.url
    val thumbnailUrl = fileProperty.thumbnailUrl?: url

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