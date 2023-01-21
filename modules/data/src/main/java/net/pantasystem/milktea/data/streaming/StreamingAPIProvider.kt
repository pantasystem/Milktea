package net.pantasystem.milktea.data.streaming

import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import net.pantasystem.milktea.api_streaming.mastodon.StreamingAPI
import net.pantasystem.milktea.api_streaming.mastodon.StreamingAPIImpl
import net.pantasystem.milktea.model.account.Account
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamingAPIProvider @Inject constructor(
    val okHttpClientProvider: OkHttpClientProvider,
) {

    private var accountAndStreamingAPIs = mutableMapOf<Long, StreamingAPI>()

    fun get(account: Account): StreamingAPI? {
        synchronized(this) {
            if (account.instanceType == Account.InstanceType.MISSKEY) {
                return null
            }
            var streaming = accountAndStreamingAPIs[account.accountId]
            if (streaming != null) {
                return streaming
            }

            streaming = StreamingAPIImpl(
                host = account.getHost(),
                token = account.token,
                okHttpClient = okHttpClientProvider.get().newBuilder().addInterceptor {
                    val request = it.request()
                    val newReq = request.newBuilder()
                        .header("Authorization", "Bearer ${account.token}")
                        .build()
                    it.proceed(newReq)
                }.build()
            )
            accountAndStreamingAPIs[account.accountId] = streaming
            return streaming
        }

    }
}