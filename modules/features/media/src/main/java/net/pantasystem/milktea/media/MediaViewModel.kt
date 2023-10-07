package net.pantasystem.milktea.media

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import net.pantasystem.milktea.common_navigation.MediaNavigationKeys
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {


    val files = savedStateHandle.getStateFlow(
        MediaNavigationKeys.EXTRA_FILES,
        emptyList<File>()
    )

}