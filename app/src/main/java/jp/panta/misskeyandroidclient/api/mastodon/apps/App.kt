package jp.panta.misskeyandroidclient.api.mastodon.apps
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URLEncoder

@Serializable
data class CreateApp(
    @SerialName("client_name")
    val clientName: String,

    @SerialName("redirect_uris")
    val redirectUris: String,
    val scopes: String
)

@Serializable
data class App(
    val id: String,
    val name: String,
    val website: String?,

    @SerialName("client_id")
    val clientId: String,

    @SerialName("redirect_uri")
    val redirectUri: String,

    @SerialName("client_secret")
    val clientSecret: String,

    @SerialName("vapid_key")
    val vapidKey: String
) {
    fun generateAuthUrl(baseURL: String, scope: String): String {
        val encodedClientId = URLEncoder.encode(clientId, "utf-8")
        val encodedRedirectUri = URLEncoder.encode(redirectUri, "utf-8")
        val encodedResponseType = URLEncoder.encode("code", "utf-8")
        val encodedScope = URLEncoder.encode(scope, "utf-8")
        return "$baseURL/oauth/authorize?client_id=${encodedClientId}&redirect_uri=$encodedRedirectUri&response_type=$encodedResponseType&code=code&scope=$encodedScope"
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
            code = code
        )
    }
}

@Serializable
data class ObtainToken(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scope: String,
    val code: String,
    val grantType: String = "authorization_code",
)

@Serializable
data class AccessToken(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("token_type")
    val tokenType: String,

    val scope: String,
    val createdAt: Long
)