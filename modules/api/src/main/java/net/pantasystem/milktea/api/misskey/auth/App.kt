package net.pantasystem.milktea.api.misskey.auth

import net.pantasystem.milktea.api.mastodon.apps.ObtainToken
import net.pantasystem.milktea.model.app.AppType
import java.io.Serializable
import java.net.URLEncoder

@kotlinx.serialization.Serializable
data class App(
    val id: String? = null,
    val name: String,
    val callbackUrl: String? = null,
    val isAuthorized: Boolean? = null,
    val permission: List<String>? = emptyList(),
    val secret: String? = null
) : Serializable {
    fun toModel(): AppType.Misskey {
        return AppType.Misskey(
            id = id,
            name = name,
            callbackUrl = callbackUrl,
            isAuthorized = isAuthorized,
            permission = permission ?: emptyList(),
            secret = secret
        )
    }
}


fun AppType.Companion.fromDTO(app: App): AppType {
    return app.toModel()
}

fun AppType.Companion.fromDTO(app: net.pantasystem.milktea.api.mastodon.apps.App): AppType {
    return app.toModel()
}

fun AppType.Mastodon.generateAuthUrl(baseURL: String, scope: String): String {
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
fun AppType.Mastodon.createObtainToken(scope: String, code: String): ObtainToken {
    return ObtainToken(
        clientId = clientId,
        clientSecret = clientSecret,
        scope = scope,
        redirectUri = redirectUri,
        code = code,
        grantType = "authorization_code"
    )
}
