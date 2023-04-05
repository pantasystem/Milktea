package net.pantasystem.milktea.api.mastodon.apps

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.app.AppType

@Serializable
data class CreateApp(
    @SerialName("client_name")
    val clientName: String,

    @SerialName("redirect_uris")
    val redirectUris: String,

    @SerialName("scopes")
    val scopes: String
)

@Serializable
data class App(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("client_id")
    val clientId: String,

    @SerialName("redirect_uri")
    val redirectUri: String,

    @SerialName("client_secret")
    val clientSecret: String,

) {
    fun toModel(): AppType.Mastodon {
        return AppType.Mastodon(
            id = id,
            name = name,
            clientSecret = clientSecret,
            clientId = clientId,
            redirectUri = redirectUri,
        )
    }

    fun toPleromaModel() : AppType.Pleroma {
        return AppType.Pleroma(
            id = id,
            name = name,
            clientSecret = clientSecret,
            clientId = clientId,
            redirectUri = redirectUri,
        )
    }
}

@Serializable
data class ObtainToken(
    @SerialName("client_id")
    val clientId: String,

    @SerialName("client_secret")
    val clientSecret: String,

    @SerialName("redirect_uri")
    val redirectUri: String,

    @SerialName("scope")
    val scope: String,

    @SerialName("code")
    val code: String,

    @SerialName("grant_type")
    val grantType: String,
)

@Serializable
data class AccessToken(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("token_type")
    val tokenType: String,

    @SerialName("scope")
    val scope: String,

    @SerialName("created_at")
    val createdAt: Long
)