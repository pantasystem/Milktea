package jp.panta.misskeyandroidclient.model.auth.custom

import jp.panta.misskeyandroidclient.api.mastodon.apps.ObtainToken
import java.io.Serializable
import java.net.URLEncoder

@kotlinx.serialization.Serializable
data class App(
    val id: String? = null,
    val name: String,
    val callbackUrl: String? = null,
    val isAuthorized: Boolean? = null,
    val permission: List<String> = emptyList(),
    val secret: String? = null
) : Serializable {
    fun toModel(): AppType.Misskey {
        return AppType.Misskey(
            id = id,
            name = name,
            callbackUrl = callbackUrl,
            isAuthorized = isAuthorized,
            permission = permission,
            secret = secret
        )
    }
}

sealed interface AppType {
    val callbackUrl: String?
    val secret: String?
    val name: String

    data class Misskey(
        val id: String? = null,
        override val name: String,
        override val callbackUrl: String?,
        val isAuthorized: Boolean? = null,
        val permission: List<String> = emptyList(),
        override val secret: String? = null
    ) : AppType

    companion object;

    data class Mastodon(
        val id: String,
        override val name: String,

        val clientId: String,

        val redirectUri: String,

        val clientSecret: String,

    ) : AppType {
        override val callbackUrl: String
            get() = redirectUri
        override val secret: String
            get() = clientSecret

        fun generateAuthUrl(baseURL: String, scope: String): String {
            val encodedClientId = URLEncoder.encode(clientId, "utf-8")
            val encodedRedirectUri = URLEncoder.encode(redirectUri, "utf-8")
            val encodedResponseType = URLEncoder.encode("code", "utf-8")
            val encodedScope = URLEncoder.encode(scope, "utf-8")
            return "$baseURL/oauth/authorize?client_id=${encodedClientId}&redirect_uri=$encodedRedirectUri&response_type=$encodedResponseType&scope=$encodedScope"
        }

        /**
         * @param scope アプリ作成時に指定したscope
         * @param code redirectUrl+codeで帰ってきたコード
         */
        fun createObtainToken(scope: String, code: String): ObtainToken {
            return ObtainToken(
                clientId = clientId,
                clientSecret = clientSecret,
                scope = scope,
                redirectUri = redirectUri,
                code = code,
                grantType = "authorization_code"
            )
        }
    }
}

fun AppType.Companion.fromDTO(app: App): AppType {
    return app.toModel()
}

fun AppType.Companion.fromDTO(app: jp.panta.misskeyandroidclient.api.mastodon.apps.App): AppType {
    return app.toModel()
}