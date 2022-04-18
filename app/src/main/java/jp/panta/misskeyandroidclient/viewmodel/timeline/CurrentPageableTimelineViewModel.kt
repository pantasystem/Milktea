package jp.panta.misskeyandroidclient.viewmodel.timeline

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import net.pantasystem.milktea.model.account.page.Pageable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

sealed interface SuitableType {
    object Gallery : SuitableType
    data class Channel(val channelId: String) : SuitableType
    object Other : SuitableType

    companion object
}

fun net.pantasystem.milktea.model.account.page.Pageable.suitableType(): SuitableType {
    return when(this) {
        is net.pantasystem.milktea.model.account.page.Pageable.ChannelTimeline -> {
            SuitableType.Channel(this.channelId)
        }
        is net.pantasystem.milktea.model.account.page.Pageable.Gallery -> {
            SuitableType.Gallery
        }
        else -> SuitableType.Other
    }
}


@HiltViewModel
class CurrentPageableTimelineViewModel @Inject constructor(

) : ViewModel() {

    private val _currentType = MutableStateFlow<net.pantasystem.milktea.model.account.page.Pageable>(
        net.pantasystem.milktea.model.account.page.Pageable.HomeTimeline())

    val currentType: StateFlow<net.pantasystem.milktea.model.account.page.Pageable> = _currentType

    fun setCurrentPageable(pageable: net.pantasystem.milktea.model.account.page.Pageable) {
        _currentType.value = pageable
    }

}