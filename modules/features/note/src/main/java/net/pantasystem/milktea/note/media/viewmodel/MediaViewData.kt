package net.pantasystem.milktea.note.media.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.file.isSensitive
import net.pantasystem.milktea.model.setting.Config

class MediaViewData(files: List<FilePreviewSource>, val config: Config?) {

    // NOTE: サイズが変わることは決してない
    private val _files = MutableLiveData(files.map{
        PreviewAbleFile(it, if (it.isSensitive) PreviewAbleFile.VisibleType.SensitiveHide else if (config?.isHideMediaWhenMobileNetwork == true) PreviewAbleFile.VisibleType.HideWhenMobileNetwork else PreviewAbleFile.VisibleType.Visible)
    })
    val files: LiveData<List<PreviewAbleFile>> = _files

    val fileOne = Transformations.map(_files) {
        it.getOrNull(0)
    }

    val fileTwo = Transformations.map(_files) {
        it.getOrNull(1)
    }

    val fileThree = Transformations.map(_files) {
        it.getOrNull(2)
    }

    val fileFour = Transformations.map(_files) {
        it.getOrNull(3)
    }

    val isOver4Files = files.size > 4
    val isVisibleMediaPreviewArea = !(isOver4Files || files.isEmpty())

    fun show(index: Int) {
        val list = (_files.value ?: emptyList()).toMutableList()
        _files.value = list.mapIndexed { i, previewAbleFile ->
            if (i == index) {
                previewAbleFile.copy(visibleType = PreviewAbleFile.VisibleType.Visible)
            } else {
                previewAbleFile
            }
        }
    }

    fun toggleVisibility(index: Int) {
        val list = (_files.value ?: emptyList()).toMutableList()
        _files.value = list.mapIndexed { i, previewAbleFile ->
            if (i == index) {
                previewAbleFile.copy(
                    visibleType = when(previewAbleFile.visibleType) {
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