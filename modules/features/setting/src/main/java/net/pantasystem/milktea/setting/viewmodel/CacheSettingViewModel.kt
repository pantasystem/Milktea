package net.pantasystem.milktea.setting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.image.ImageCacheRepository
import net.pantasystem.milktea.model.note.NoteDataSource
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject

@HiltViewModel
class CacheSettingViewModel @Inject constructor(
    private val imageCacheRepository: ImageCacheRepository,
    private val noteDataSource: NoteDataSource,
    private val userDataSource: UserDataSource,
    loggerFac: Logger.Factory,
) : ViewModel() {

    private val logger = loggerFac.create("CacheSettingVM")

    private val _refreshEvent = MutableStateFlow(0L)

    val uiState = _refreshEvent.map {
        CacheSettingUiState(
            imageCacheSize = runCancellableCatching { imageCacheRepository.findCachedFileCount()}.getOrElse { 0L },
            noteCacheSize = noteDataSource.findLocalCount().getOrElse { 0L },
            userCacheSize = userDataSource.count().getOrElse { 0L },
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CacheSettingUiState(),
    )


    fun onClearNoteCache() {
        viewModelScope.launch {
            noteDataSource.clear().onFailure { e ->
                logger.error("Failed to clear note cache", e)
            }
            _refreshEvent.value = System.currentTimeMillis()
        }
    }

    fun onClearCustomEmojiCache() {
        viewModelScope.launch {
            try {
                imageCacheRepository.clear()
            } catch (e: Exception) {
                logger.error("Failed to clear custom emoji cache", e)
            }
            _refreshEvent.value = System.currentTimeMillis()
        }
    }

    fun onClearUserCache() {
        viewModelScope.launch {
            userDataSource.clear().onFailure { e->
                logger.error("Failed to clear user cache", e)
            }
            _refreshEvent.value = System.currentTimeMillis()
        }
    }
}

data class CacheSettingUiState(
    val imageCacheSize: Long = 0L,
    val noteCacheSize: Long = 0L,
    val userCacheSize: Long = 0L
)