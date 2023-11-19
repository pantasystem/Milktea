package net.pantasystem.milktea.note.media.viewmodel

import net.pantasystem.milktea.model.file.AboutMediaType
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.setting.MediaDisplayMode


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

    fun isHidingWithNetworkStateAndConfig(isMobileNetwork: Boolean, mediaDisplayMode: MediaDisplayMode): Boolean {
        return when(visibleType) {
            VisibleType.Visible -> false
            VisibleType.HideWhenMobileNetwork -> {
                if (mediaDisplayMode == MediaDisplayMode.ALWAYS_HIDE_WHEN_MOBILE_NETWORK) {
                    isMobileNetwork
                } else mediaDisplayMode == MediaDisplayMode.ALWAYS_HIDE
            }
            VisibleType.SensitiveHide -> true
        }
    }

    enum class VisibleType {
        Visible,
        HideWhenMobileNetwork,
        SensitiveHide,
    }
}