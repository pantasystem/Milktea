package net.pantasystem.milktea.note.media.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.file.isSensitive
import net.pantasystem.milktea.model.setting.Config

class MediaViewData(
    files: List<FilePreviewSource>,
    val config: Config?,
    coroutineScope: CoroutineScope,
) {

    // NOTE: サイズが変わることは決してない
    private val _files = MutableStateFlow(files.map {
        PreviewAbleFile(
            it,
            if (it.isSensitive)
                PreviewAbleFile.VisibleType.SensitiveHide
            else if (config?.isHideMediaWhenMobileNetwork == true)
                PreviewAbleFile.VisibleType.HideWhenMobileNetwork
            else
                PreviewAbleFile.VisibleType.Visible
        )
    })
    val files: StateFlow<List<PreviewAbleFile>> = _files

    val isHideFourMediaPreviewLayout = _files.map { it.isEmpty() || it.size > 4 }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), true)

    val fileOne = _files.map {
        it.getOrNull(0)
    }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), null)

    val fileTwo = _files.map {
        it.getOrNull(1)
    }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), null)

    val fileThree = _files.map {
        it.getOrNull(2)
    }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), null)

    val fileFour = _files.map {
        it.getOrNull(3)
    }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), null)

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

    fun toggleVisibility(index: Int) {
        _files.update {
            it.toMutableList().mapIndexed { i, previewAbleFile ->
                if (i == index) {
                    previewAbleFile.copy(
                        visibleType = when (previewAbleFile.visibleType) {
                            PreviewAbleFile.VisibleType.Visible -> PreviewAbleFile.VisibleType.SensitiveHide
                            PreviewAbleFile.VisibleType.HideWhenMobileNetwork -> PreviewAbleFile.VisibleType.Visible
                            PreviewAbleFile.VisibleType.SensitiveHide -> PreviewAbleFile.VisibleType.Visible
                        }
                    )
                } else {
                    previewAbleFile
                }
            }
        }
    }

}