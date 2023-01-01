package net.pantasystem.milktea.note.media.viewmodel

import net.pantasystem.milktea.model.file.File
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.file.toFile


data class PreviewAbleFile(val source: FilePreviewSource, val isHiding: Boolean) {
    val file: File = when(source) {
        is FilePreviewSource.Local -> source.file.toFile()
        is FilePreviewSource.Remote -> source.fileProperty.toFile()
    }
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


    private val isVideo = type == Type.VIDEO
    val isVisiblePlayButton: Boolean
        get() = isVideo && !isHiding

}