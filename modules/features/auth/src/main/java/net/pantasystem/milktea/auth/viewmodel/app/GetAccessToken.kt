package net.pantasystem.milktea.auth.viewmodel.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.api.misskey.auth.UserKey
import net.pantasystem.milktea.api.misskey.auth.createObtainToken
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import net.pantasystem.milktea.data.infrastructure.auth.custom.AccessToken
import net.pantasystem.milktea.data.infrastructure.auth.custom.toModel
import javax.inject.Inject


class GetAccessToken @Inject constructor(
    val mastodonAPIProvider: MastodonAPIProvider,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val misskeyAPIServiceBuilder: MisskeyAPIServiceBuilder,
    loggerFactory: Logger.Factory,
) {

    val logger by lazy {
        loggerFactory.create("GetAccessToken")
    }

    suspend fun getAccessToken(a: Authorization.Waiting4UserAuthorization, code: String? = null): Result<AccessToken> {
        return runCancellableCatching {
            withContext(Dispatchers.IO) {
                when (a) {
                    is Authorization.Waiting4UserAuthorization.Misskey -> {
                        val accessToken =
                            misskeyAPIServiceBuilder.buildAuthAPI(a.instanceBaseURL).getAccessToken(
                                UserKey(
                                    appSecret = a.appSecret,
                                    a.session.token
                                )
                            )
                                .throwIfHasError().body()
                                ?: throw IllegalStateException("response bodyがありません。")
                        accessToken.toModel(a.appSecret)
                    }
                    is Authorization.Waiting4UserAuthorization.Mastodon -> {
                        getAccessToken4Mastodon(a, code!!)
                    }
                }
            }
        }
    }
    private suspend fun getAccessToken4Mastodon(
        a: Authorization.Waiting4UserAuthorization.Mastodon,
        code: String
    ): AccessToken.Mastodon {
        try {
            logger.debug { "認証種別Mastodon: $a" }
            val obtainToken = a.client.createObtainToken(scope = a.scope, code = code)
            val accessToken = mastodonAPIProvider.get(a.instanceBaseURL).obtainToken(obtainToken)
                .throwIfHasError()
                .body()
            logger.debug { "accessToken:$accessToken" }
            val me = mastodonAPIProvider.get(a.instanceBaseURL, accessToken!!.accessToken)
                .verifyCredentials()
                .throwIfHasError()
            logger.debug { "自身の情報, code=${me.code()}, message=${me.message()}" }
            val account = me.body()!!
            return accessToken.toModel(account)
        } catch (e: Exception) {
            logger.warning("AccessToken取得失敗", e = e)
            throw e
        }
    }
}