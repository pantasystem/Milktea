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
    val scopes: String
)

@Serializable
data class App(
    val id: String,
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
}

@Serializable
data class ObtainToken(
    @SerialName("client_id") val clientId: String,
    @SerialName("client_secret") val clientSecret: String,
    @SerialName("redirect_uri") val redirectUri: String,
    val scope: String,
    val code: String,
    @SerialName("grant_type") val grantType: String,
)

@Serializable
data class AccessToken(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("token_type")
    val tokenType: String,

    val scope: String,
    @SerialName("created_at")
    val createdAt: Long
)