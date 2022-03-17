package jp.panta.misskeyandroidclient.streaming.channel

import jp.panta.misskeyandroidclient.streaming.ChannelBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter


fun ChannelAPI.connectUserTimeline(userId: String): Flow<ChannelBody> {
    return this.connect(ChannelAPI.Type.Global).filter {
        it is ChannelBody.ReceiveNote && it.body.userId == userId
    }
}