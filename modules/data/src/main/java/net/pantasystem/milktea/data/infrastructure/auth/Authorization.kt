package net.pantasystem.milktea.data.infrastructure.auth


import net.pantasystem.milktea.api.misskey.auth.Session
import net.pantasystem.milktea.api.misskey.auth.generateAuthUrl
import net.pantasystem.milktea.data.infrastructure.auth.custom.AccessToken
import net.pantasystem.milktea.data.infrastructure.auth.custom.TemporarilyAuthState
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.app.AppType
import net.pantasystem.milktea.model.user.User


/**
 * 認証の状態
 */
sealed interface Authorization {
    object BeforeAuthentication : Authorization

    sealed interface Waiting4UserAuthorization : Authorization{
        val instanceBaseURL: String
        fun generateAuthUrl(): String

        companion object;

        data class Misskey(
            override val instanceBaseURL: String,
            val viaName: String?,
            val appSecret: String,
            val session: Session
        ) : Waiting4UserAuthorization {
            override fun generateAuthUrl(): String {
                return session.url
            }
        }

        data class Mastodon(
            override val instanceBaseURL: String,
            val client: AppType.Mastodon,
            val scope: String,
        ) : Waiting4UserAuthorization {
            override fun generateAuthUrl(): String {
                return client.generateAuthUrl(instanceBaseURL, scope)
            }
        }

        data class Pleroma(
            override val instanceBaseURL: String,
            val client: AppType.Pleroma,
            val scope: String
        ) : Waiting4UserAuthorization {
            override fun generateAuthUrl(): String {
                return client.generateAuthUrl(instanceBaseURL, scope)
            }
        }

        data class Firefish(
            override val instanceBaseURL: String,
            val viaName: String?,
            val appSecret: String,
            val session: Session
        ) : Waiting4UserAuthorization {
            override fun generateAuthUrl(): String {
                return session.url
            }
        }

    }


    data class Approved(
        val instanceBaseURL: String,
        val accessToken: AccessToken
    ) : Authorization

    data class Finish(
        val account: Account, val user: User
    ) : Authorization

}

fun Authorization.Waiting4UserAuthorization.Companion.from(state: TemporarilyAuthState): Authorization.Waiting4UserAuthorization {
    return when(state) {
        is TemporarilyAuthState.Mastodon -> {
            Authorization.Waiting4UserAuthorization.Mastodon(
                client = state.app,
                instanceBaseURL = state.instanceDomain,
                scope = state.scope
            )
        }
        is TemporarilyAuthState.Misskey -> {
            Authorization.Waiting4UserAuthorization.Misskey(
                appSecret = state.secret,
                session = state.session,
                instanceBaseURL = state.instanceDomain,
                viaName = state.viaName
            )
        }
        is TemporarilyAuthState.Pleroma -> {
            Authorization.Waiting4UserAuthorization.Pleroma(
                client = state.app,
                instanceBaseURL = state.instanceDomain,
                scope = state.scope
            )
        }
        is TemporarilyAuthState.Firefish-> {
            Authorization.Waiting4UserAuthorization.Firefish(
                appSecret = state.secret,
                session = state.session,
                instanceBaseURL = state.instanceDomain,
                viaName = state.viaName
            )
        }
    }
}