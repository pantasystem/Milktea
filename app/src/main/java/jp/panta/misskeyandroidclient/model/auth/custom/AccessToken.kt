package jp.panta.misskeyandroidclient.model.auth.custom

import jp.panta.misskeyandroidclient.api.misskey.users.UserDTO
import jp.panta.misskeyandroidclient.api.misskey.auth.AccessToken as MisskeyAccessToken
import jp.panta.misskeyandroidclient.api.mastodon.apps.AccessToken as MastodonAccessToken
sealed interface AccessToken {
    val accessToken: String
    data class Misskey(
        override val accessToken: String,
        val user: UserDTO
    ) : AccessToken

    data class Mastodon(
        override val accessToken: String,
        val tokenType: String,
        val scope: String,
        val createdAt: Long
    ) : AccessToken
}


fun MisskeyAccessToken.toModel() : AccessToken.Misskey {
    return AccessToken.Misskey(
        accessToken = accessToken,
        user = user
    )
}

fun MastodonAccessToken.toModel() : AccessToken.Mastodon {
    return AccessToken.Mastodon(
        accessToken = accessToken,
        tokenType = tokenType,
        createdAt = createdAt,
        scope = scope
    )
}