package net.pantasystem.milktea.data.api.mastodon

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.mastodon.MastodonAPI
import net.pantasystem.milktea.model.account.Account
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MastodonAPIProvider @Inject constructor(
    private val mastodonAPIFactory: MastodonAPIFactory,
){

    data class Key(
        val instanceBaseURL: String,
        val token: String?,
    )

    private val lock = Mutex()
    private val apiMap = mutableMapOf<Key, MastodonAPI>()
    suspend fun get(baseURL: String): MastodonAPI {
        return lock.withLock {
            var api = apiMap[Key(baseURL, null)]
            if (api != null) {
                return api
            }
            api = mastodonAPIFactory.build(baseURL, null)
            apiMap[Key(baseURL, null)] = api
            return@withLock api
        }
    }

    suspend fun get(account: Account): MastodonAPI {
        if (account.instanceType == Account.InstanceType.MISSKEY) {
            throw IllegalArgumentException("アカウント種別Misskeyは受け入れていません")
        }
        return get(account.normalizedInstanceUri, account.token)

    }

    suspend fun get(baseURL: String, token: String): MastodonAPI {

        val key = Key(baseURL, token)
        lock.withLock {
            var api = apiMap[key]
            if (api != null) {
                return api
            }
            api = mastodonAPIFactory.build(baseURL, token)
            apiMap[key] = api
            return api
        }
    }
}