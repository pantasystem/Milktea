package net.pantasystem.milktea.note.media.viewmodel

import net.pantasystem.milktea.model.file.AboutMediaType
import net.pantasystem.milktea.model.file.FilePreviewSource


data class PreviewAbleFile(val source: FilePreviewSource, val isHiding: Boolean) {


    private val type = this.source.aboutMediaType


    private val isVideo = type == AboutMediaType.VIDEO
    val isVisiblePlayButton: Boolean
        get() = isVideo && !isHiding

}