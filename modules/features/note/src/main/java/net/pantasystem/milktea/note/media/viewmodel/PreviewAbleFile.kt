package net.pantasystem.milktea.note.media.viewmodel

import net.pantasystem.milktea.model.file.AboutMediaType
import net.pantasystem.milktea.model.file.FilePreviewSource


data class PreviewAbleFile(
    val source: FilePreviewSource,
    val visibleType: VisibleType,
    val initialVisibleType: VisibleType = visibleType
) {


    private val type = this.source.aboutMediaType


    private val isVideo = type == AboutMediaType.VIDEO
    val isHiding = visibleType == VisibleType.SensitiveHide
    val isVisiblePlayButton: Boolean
        get() = isVideo && !isHiding

    enum class VisibleType {
        Visible,
        HideWhenMobileNetwork,
        SensitiveHide,
    }
}