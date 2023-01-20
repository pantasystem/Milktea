package net.pantasystem.milktea.data.streaming

import net.pantasystem.milktea.api_streaming.mastodon.StreamingAPI
import net.pantasystem.milktea.api_streaming.mastodon.StreamingAPIImpl
import net.pantasystem.milktea.model.account.Account
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamingAPIProvider @Inject constructor() {

    private var accountAndStreamingAPIs = mutableMapOf<Long, StreamingAPI>()

    fun get(account: Account): StreamingAPI {
        synchronized(this) {
            var streaming = accountAndStreamingAPIs[account.accountId]
            if (streaming != null) {
                return streaming
            }

            streaming = StreamingAPIImpl()
            accountAndStreamingAPIs[account.accountId] = streaming
            return streaming
        }

    }
}