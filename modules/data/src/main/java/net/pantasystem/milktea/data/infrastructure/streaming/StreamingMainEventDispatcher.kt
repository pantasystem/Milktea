package net.pantasystem.milktea.data.infrastructure.streaming

import net.pantasystem.milktea.api_streaming.ChannelBody
import net.pantasystem.milktea.model.account.Account


interface StreamingMainEventDispatcher {

    suspend fun dispatch(account: Account, mainEvent: ChannelBody.Main): Boolean
}