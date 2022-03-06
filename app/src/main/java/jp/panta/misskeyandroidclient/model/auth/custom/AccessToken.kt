package jp.panta.misskeyandroidclient.model.auth.custom

import jp.panta.misskeyandroidclient.api.mastodon.accounts.MastodonAccountDTO
import jp.panta.misskeyandroidclient.api.misskey.users.UserDTO
import jp.panta.misskeyandroidclient.api.misskey.auth.AccessToken as MisskeyAccessToken
import jp.panta.misskeyandroidclient.api.mastodon.apps.AccessToken as MastodonAccessToken


sealed interface AccessToken {
    val accessToken: String
    data class Misskey(
        var appSecret: String,
        override val accessToken: String,
        val user: UserDTO
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