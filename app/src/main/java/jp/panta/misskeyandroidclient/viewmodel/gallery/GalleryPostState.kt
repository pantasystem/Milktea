package jp.panta.misskeyandroidclient.viewmodel.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.gallery.GalleryDataSource
import jp.panta.misskeyandroidclient.model.gallery.GalleryPost
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class GalleryPostState(
    val galleryPost: GalleryPost,
    val files: List<FileProperty>,
    val user: User,
    val gallerySendToggleLikeOrUnlike: GalleryToggleLikeOrUnlike,
    val galleryDataSource: GalleryDataSource,
    val coroutineScope: CoroutineScope,
    val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    currentIndex: Int = 0
) {

    init {
        require(files.isNotEmpty())
        galleryDataSource.events().filter {
            it.galleryPostId == galleryPost.id
        }.mapNotNull {
            (it as? GalleryDataSource.Event.Updated)?.galleryPost
                ?: (it as? GalleryDataSource.Event.Created)?.galleryPost
        }.mapNotNull {
            it as GalleryPost.Authenticated
        }.onEach {
            _isLiked.postValue(it.isLiked)
        }.launchIn(coroutineScope + dispatcher)
    }

    private val _isLiked = MutableLiveData<Boolean>((galleryPost as? GalleryPost.Authenticated)?.isLiked)
    val isLiked: LiveData<Boolean> = _isLiked

    private val _isSending = MutableLiveData<Boolean>(false)
    val isSending: LiveData<Boolean> = _isSending

    private val _currentIndex = MutableStateFlow(currentIndex)
    val currentIndex: StateFlow<Int> = _currentIndex

    val fileViewDataList = files.map {
        FileViewData(it.toFile())
    }

    fun setCurrentIndex(index: Int) {
        _currentIndex.value = index
    }

    fun toggleFavorite() {
        coroutineScope.launch(dispatcher) {
            _isSending.postValue(true)
            runCatching {
                gallerySendToggleLikeOrUnlike.toggle(galleryPost.id)
            }
            _isSending.postValue(false)
        }
    }
}