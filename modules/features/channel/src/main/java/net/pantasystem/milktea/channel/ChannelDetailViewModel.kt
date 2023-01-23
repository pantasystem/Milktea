package net.pantasystem.milktea.channel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.channel.ChannelRepository
import net.pantasystem.milktea.model.channel.ChannelStateModel
import javax.inject.Inject

@HiltViewModel
class ChannelDetailViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    channelStateModel: ChannelStateModel,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {


    val channelId by lazy {
        ChannelDetailArgs(savedStateHandle).channelId
    }

    val channel = channelStateModel.observeOne(channelId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            channelRepository.findOne(channelId)
        }
    }
}

class ChannelDetailArgs(savedStateHandle: SavedStateHandle) {
    companion object {
        const val accountId = "accountId"
        const val channelId = "channelId"
    }

    val channelId: Channel.Id by lazy {
        Channel.Id(
            accountId = savedStateHandle["accountId"]!!,
            channelId = savedStateHandle["channelId"]!!
        )
    }
}