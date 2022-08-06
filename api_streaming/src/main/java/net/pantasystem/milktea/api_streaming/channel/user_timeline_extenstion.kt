package net.pantasystem.milktea.api_streaming.channel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import net.pantasystem.milktea.api_streaming.ChannelBody


fun ChannelAPI.connectUserTimeline(userId: String): Flow<ChannelBody> {
    return this.connect(ChannelAPI.Type.Global).filter {
        it is ChannelBody.ReceiveNote && it.body.userId == userId
    }
}