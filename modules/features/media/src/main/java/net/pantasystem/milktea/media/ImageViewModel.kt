package net.pantasystem.milktea.media

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val EXTRA_IMAGE_URL = "jp.panta.misskeyandroidclient.ui.media.EXTRA_IMAGE_URL"
        const val EXTRA_IMAGE_URI = "jp.panta.misskeyandroidclient.ui.media.EXTRA_IMAGE_URI"
        const val EXTRA_IMAGE_THUMBNAIL_URL = "jp.panta.misskeyandroidclient.ui.media.EXTRA_IMAGE_THUMBNAIL_URL"
    }


    private val _isImageLoading = MutableStateFlow(true)
    val isImageLoading = _isImageLoading.asStateFlow()

    fun onResourceReady() {
        viewModelScope.launch {
            delay(100)
            _isImageLoading.value = false
        }
    }

    fun onLoadFailed() {
        viewModelScope.launch {
            delay(100)
            _isImageLoading.value = false
        }
    }

    fun getThumbnailUrl(): String? {
        return savedStateHandle[EXTRA_IMAGE_THUMBNAIL_URL]
    }

    fun getUri(): String? {
        return savedStateHandle[EXTRA_IMAGE_URI] ?: savedStateHandle[EXTRA_IMAGE_URL]
    }
}
