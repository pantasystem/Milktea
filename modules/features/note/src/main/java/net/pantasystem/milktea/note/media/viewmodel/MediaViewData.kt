package net.pantasystem.milktea.note.media.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.file.isSensitive
import net.pantasystem.milktea.model.setting.Config
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.MediaDisplayMode

class MediaViewData(
    files: List<FilePreviewSource>,
    val config: Config?,
) {

    // NOTE: サイズが変わることは決してない
    private val _files = MutableStateFlow(
        files.map {
            PreviewAbleFile(
                it,
                if (it.isSensitive) {
                    if (config?.mediaDisplayMode == MediaDisplayMode.ALWAYS_SHOW) {
                        PreviewAbleFile.VisibleType.Visible
                    } else {
                        PreviewAbleFile.VisibleType.SensitiveHide
                    }
                } else if (when (config?.mediaDisplayMode
                        ?: DefaultConfig.config.mediaDisplayMode) {
                        MediaDisplayMode.AUTO -> false
                        MediaDisplayMode.ALWAYS_HIDE -> true
                        MediaDisplayMode.ALWAYS_HIDE_WHEN_MOBILE_NETWORK -> true
                        MediaDisplayMode.ALWAYS_SHOW -> false
                    }
                ) {
                    PreviewAbleFile.VisibleType.HideWhenMobileNetwork
                } else {
                    PreviewAbleFile.VisibleType.Visible
                }
            )
        },
    )
    val files: StateFlow<List<PreviewAbleFile>> = _files

    val isOver4Files = files.size > 4
    val isVisibleMediaPreviewArea = !(isOver4Files || files.isEmpty())

    fun show(index: Int) {
        _files.update { list ->
            list.mapIndexed { i, previewAbleFile ->
                if (i == index) {
                    previewAbleFile.copy(visibleType = PreviewAbleFile.VisibleType.Visible)
                } else {
                    previewAbleFile
                }
            }
        }
    }

    fun toggleVisibility(index: Int, isMobileNetwork: Boolean, mediaDisplayMode: MediaDisplayMode) {
        _files.update {
            it.toMutableList().mapIndexed { i, previewAbleFile ->
                if (i == index) {
                    previewAbleFile.copy(
                        visibleType = if (previewAbleFile.isHidingWithNetworkStateAndConfig(
                                isMobileNetwork = isMobileNetwork,
                                mediaDisplayMode = mediaDisplayMode,
                            )
                        ) {
                            PreviewAbleFile.VisibleType.Visible
                        } else {
                            PreviewAbleFile.VisibleType.SensitiveHide
                        }
                    )
                } else {
                    previewAbleFile
                }
            }
        }
    }

}