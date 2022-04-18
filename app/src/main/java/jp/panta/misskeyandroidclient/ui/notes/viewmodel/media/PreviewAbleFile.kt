package jp.panta.misskeyandroidclient.ui.notes.viewmodel.media

import net.pantasystem.milktea.model.file.File


data class PreviewAbleFile(val file: File, val isHiding: Boolean) {
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


    val isImage = type == Type.IMAGE
    val isVideo = type == Type.VIDEO
    val isVisiblePlayButton: Boolean
        get() = isVideo && !isHiding

}