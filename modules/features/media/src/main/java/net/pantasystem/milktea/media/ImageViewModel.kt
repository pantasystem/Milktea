package net.pantasystem.milktea.media

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val EXTRA_IMAGE_URL = "jp.panta.misskeyandroidclient.ui.media.EXTRA_IMAGE_URL"
        const val EXTRA_IMAGE_URI = "jp.panta.misskeyandroidclient.ui.media.EXTRA_IMAGE_URI"
    }

    private val url = savedStateHandle.getStateFlow<String?>(
        EXTRA_IMAGE_URL,
        null,
    )

    private val uri = savedStateHandle.getStateFlow<String?>(
        EXTRA_IMAGE_URI,
        null,
    )

    private val isImageLoading = MutableStateFlow(true)

    val uiState = combine(url, uri, isImageLoading) { url, uri, loading ->
        ImageUiState(
            url = url,
            uri = uri,
            isLoading = loading,
        )
    }

    fun onResourceReady() {
        isImageLoading.value = false
    }

    fun onLoadFailed() {
        isImageLoading.value = false
    }
}

data class ImageUiState(
    val uri: String?,
    val url: String?,
    val isLoading: Boolean = false,
)