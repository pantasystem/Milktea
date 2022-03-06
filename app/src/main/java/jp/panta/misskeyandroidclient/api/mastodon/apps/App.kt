package jp.panta.misskeyandroidclient.api.mastodon.apps
import jp.panta.misskeyandroidclient.model.auth.custom.AppType
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
    fun toModel(): AppType.Mastodon {
        return AppType.Mastodon(
            id = id,
            name = name,
            website = website,
            clientSecret = clientSecret,
            clientId = clientId,
            redirectUri = redirectUri,
            vapidKey = vapidKey
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