package net.pantasystem.milktea.data.model.streaming

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.data.streaming.ChannelBody


interface StreamingMainEventDispatcher {

    suspend fun dispatch(account: net.pantasystem.milktea.model.account.Account, mainEvent: ChannelBody.Main): Boolean
}