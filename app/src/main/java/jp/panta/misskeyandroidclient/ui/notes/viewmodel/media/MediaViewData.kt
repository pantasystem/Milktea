package jp.panta.misskeyandroidclient.ui.notes.viewmodel.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import jp.panta.misskeyandroidclient.model.file.File

class MediaViewData(files: List<File>) {

    // NOTE: サイズが変わることは決してない
    private val _files = MutableLiveData(files.map{
        PreviewAbleFile(it, it.isSensitive ?: false)
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
                previewAbleFile.copy(isHiding = false)
            } else {
                previewAbleFile
            }
        }
    }

    fun show(previewAbleFile: PreviewAbleFile) {
        val list = (_files.value ?: emptyList()).toMutableList()
        _files.value = list.map {
            if (it === previewAbleFile) {
                it.copy(isHiding = false)
            } else {
                it
            }
        }
    }

    fun toggleVisibility(index: Int) {
        val list = (_files.value ?: emptyList()).toMutableList()
        _files.value = list.mapIndexed { i, previewAbleFile ->
            if (i == index) {
                previewAbleFile.copy(isHiding = !previewAbleFile.isHiding)
            } else {
                previewAbleFile
            }
        }
    }

    fun toggleVisibility(previewAbleFile: PreviewAbleFile) {
        val list = (_files.value ?: emptyList()).toMutableList()
        _files.value = list.map {
            if (it === previewAbleFile) {
                it.copy(isHiding = !it.isHiding)
            } else {
                it
            }
        }
    }
}