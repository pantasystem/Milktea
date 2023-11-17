package net.pantasystem.milktea.common_viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.pantasystem.milktea.model.account.page.Pageable
import javax.inject.Inject

sealed interface SuitableType {
    data object Gallery : SuitableType
    data class Channel(val channelId: String) : SuitableType
    data object Other : SuitableType

    companion object
}

fun Pageable.suitableType(): SuitableType {
    return when(this) {
        is Pageable.ChannelTimeline -> {
            SuitableType.Channel(this.channelId)
        }
        is Pageable.Gallery -> {
            SuitableType.Gallery
        }
        else -> SuitableType.Other
    }
}


@HiltViewModel
class CurrentPageableTimelineViewModel @Inject constructor(

) : ViewModel() {

    private val _currentType = MutableStateFlow<CurrentPageType>(
        CurrentPageType.Page(null, Pageable.HomeTimeline(), null))

    val currentType: StateFlow<CurrentPageType> = _currentType


    private val _currentAccountId = MutableStateFlow<Long?>(null)
    val currentAccountId = _currentAccountId.asStateFlow()

    fun setCurrentPageable(accountId: Long?, pageable: Pageable, pageId: Long? = null,) {
        _currentType.value = CurrentPageType.Page(accountId, pageable, pageId)
    }

    fun setCurrentPageType(type: CurrentPageType) {
        _currentType.value = type
    }

}

sealed interface CurrentPageType {
    data class Page(val accountId: Long?, val pageable: Pageable, val pageId: Long?) : CurrentPageType
    data object Account : CurrentPageType
}