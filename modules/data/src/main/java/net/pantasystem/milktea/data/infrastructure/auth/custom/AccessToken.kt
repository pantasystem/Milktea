package net.pantasystem.milktea.data.infrastructure.auth.custom

import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.api.mastodon.apps.AccessToken as MastodonAccessToken
import net.pantasystem.milktea.api.misskey.auth.AccessToken as MisskeyAccessToken


sealed interface AccessToken {
    val accessToken: String
    data class Misskey(
        var appSecret: String,
        override val accessToken: String,
        val user: UserDTO
    ) : AccessToken

    data class MisskeyIdAndPassword(
        val baseUrl: String,
        override val accessToken: String,
        val user: UserDTO,
    ) : AccessToken

    data class Mastodon(
        override val accessToken: String,
        val tokenType: String,
        val scope: String,
        val createdAt: Long,
        val account: MastodonAccountDTO
    ) : AccessToken
}


fun MisskeyAccessToken.toModel(appSecret: String) : AccessToken.Misskey {
    return AccessToken.Misskey(
        accessToken = accessToken,
        user = user,
        appSecret = appSecret
    )
}

fun MastodonAccessToken.toModel(account: MastodonAccountDTO) : AccessToken.Mastodon {
    return AccessToken.Mastodon(
        accessToken = accessToken,
        tokenType = tokenType,
        createdAt = createdAt,
        scope = scope,
        account = account
    )
}