package net.pantasystem.milktea.data.streaming.channel

import net.pantasystem.milktea.data.streaming.ChannelBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter


fun ChannelAPI.connectUserTimeline(userId: String): Flow<ChannelBody> {
    return this.connect(ChannelAPI.Type.Global).filter {
        it is ChannelBody.ReceiveNote && it.body.userId == userId
    }
}