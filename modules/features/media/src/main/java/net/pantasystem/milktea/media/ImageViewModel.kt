package net.pantasystem.milktea.media

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val EXTRA_IMAGE_URL = "jp.panta.misskeyandroidclient.ui.media.EXTRA_IMAGE_URL"
        const val EXTRA_IMAGE_URI = "jp.panta.misskeyandroidclient.ui.media.EXTRA_IMAGE_URI"
        const val EXTRA_IMAGE_THUMBNAIL_URL = "jp.panta.misskeyandroidclient.ui.media.EXTRA_IMAGE_THUMBNAIL_URL"
    }

    private val url = savedStateHandle.getStateFlow<String?>(
        EXTRA_IMAGE_URL,
        null,
    )

    private val uri = savedStateHandle.getStateFlow<String?>(
        EXTRA_IMAGE_URI,
        null,
    )

    private val thumbnailUrl = savedStateHandle.getStateFlow<String?>(
        EXTRA_IMAGE_THUMBNAIL_URL,
        null,
    )

    private val _isImageLoading = MutableStateFlow(true)
    val isImageLoading = _isImageLoading.asStateFlow()

    val uiState = combine(url, uri, thumbnailUrl) { url, uri, thumbnail ->
        ImageUiState(
            uri = url ?: uri,
            thumbnailUrl = thumbnail,
        )
    }

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
}

data class ImageUiState(
    val uri: String?,
    val thumbnailUrl: String?,
)