package jp.panta.misskeyandroidclient.viewmodel.gallery

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.gallery.GalleryPost
import jp.panta.misskeyandroidclient.model.gallery.GalleryRepository
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


sealed class EditType {
    data class Update(
        val postId: GalleryPost.Id
    ) : EditType()

    data class Create(
        val accountId: Long?
    ) : EditType()
}

class GalleryEditorViewModel(
    val editType: EditType,
    val galleryRepository: GalleryRepository,
    val filePropertyDataSource: FilePropertyDataSource,
    val logger: Logger
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val editType: EditType, val miCore: MiCore) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GalleryEditorViewModel(
                editType,
                miCore.getGalleryRepository(),
                miCore.getFilePropertyDataSource(),
                miCore.loggerFactory.create("GalleryEditorVM")
            ) as T
        }
    }

    private val _title = MutableLiveData<String>()
    val title = _title

    private val _description = MutableLiveData<String>()
    val description = _description

    private val _pickedImages = MutableLiveData<List<File>>()
    val pickedImages: LiveData<List<File>> = _pickedImages


    init {
        viewModelScope.launch(Dispatchers.IO) {
            if(editType is EditType.Update) {
                runCatching {
                    fetchWithApply(editType.postId)
                }.onFailure {
                    logger.debug("Update用のGalleryPostの取得に失敗した", e = it)
                }

            }
        }
    }

    private suspend fun fetchWithApply(postId: GalleryPost.Id) {
        val galleryPost = galleryRepository.find(postId)
        _title.postValue(galleryPost.title)
        _description.postValue(galleryPost.description)
        val files = filePropertyDataSource.findIn(galleryPost.fileIds)

        _pickedImages.postValue(
            files.map {
                it.toFile()
            }
        )
    }
}