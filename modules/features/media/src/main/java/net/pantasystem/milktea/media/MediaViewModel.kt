package net.pantasystem.milktea.media

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import net.pantasystem.milktea.common_navigation.MediaNavigationKeys
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {


    private val files = savedStateHandle.getStateFlow<List<File>?>(
        MediaNavigationKeys.EXTRA_FILES,
        null
    )

    private val file = savedStateHandle.getStateFlow<File?>(
        MediaNavigationKeys.EXTRA_FILE,
        null,
    )

    val medias = combine(files, file) { files , file ->
        when {
            !files.isNullOrEmpty() -> {
                files.map {
                    Media.FileMedia(it)
                }
            }
            file != null -> {
                listOf<Media>(Media.FileMedia(file))
            }
            else -> {
                Log.e(MediaNavigationKeys.TAG, "params must not null")
                throw IllegalArgumentException()
            }
        }
    }

    private val currentIndex = savedStateHandle.getStateFlow<Int>(
        MediaNavigationKeys.EXTRA_FILE_CURRENT_INDEX,
        0,
    )

    val uiState = combine(medias, currentIndex) { medias, currentIndex ->
        MediaUiState(medias, currentIndex)
    }

    fun setCurrentIndex(index: Int) {
        savedStateHandle[MediaNavigationKeys.EXTRA_FILE_CURRENT_INDEX] = index
    }
}

data class MediaUiState(
    val medias: List<Media>,
    val currentIndex: Int,
)