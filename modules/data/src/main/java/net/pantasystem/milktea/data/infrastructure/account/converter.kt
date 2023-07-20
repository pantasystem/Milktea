package net.pantasystem.milktea.data.infrastructure.account

import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.common.Hash
import net.pantasystem.milktea.data.infrastructure.auth.custom.AccessToken
import net.pantasystem.milktea.model.account.Account


fun AccessToken.Misskey.newAccount(instanceDomain: String): Account {
    return this.user.newAccount(
        instanceDomain,
        Hash.sha256(accessToken + appSecret)
    )
}

fun AccessToken.Firefish.newAccount(instanceDomain: String): Account {
    return this.user.newAccount(
        instanceDomain,
        Hash.sha256(accessToken + appSecret)
    )
}

fun AccessToken.Mastodon.newAccount(
    instanceDomain: String,
): Account {
    return Account(
        remoteId = this.account.id,
        userName = this.account.username,
        instanceDomain = instanceDomain,
        token = accessToken,
        instanceType = Account.InstanceType.MASTODON,
        pages = emptyList()
    )
}

fun AccessToken.Pleroma.newAccount(
    instanceDomain: String
): Account {
    return Account(
        remoteId = this.account.id,
        userName = this.account.username,
        instanceDomain = instanceDomain,
        token = accessToken,
        instanceType = Account.InstanceType.PLEROMA,
        pages = emptyList()
    )
}

fun AccessToken.MisskeyIdAndPassword.newAccount(instanceDomain: String): Account {
    return this.user.newAccount(
        instanceDomain,
        accessToken,
    )
}

fun AccessToken.newAccount(instanceDomain: String): Account {
    return when(this) {
        is AccessToken.Misskey -> {
            this.newAccount(instanceDomain)
        }
        is AccessToken.Mastodon -> {
            this.newAccount(instanceDomain)
        }
        is AccessToken.MisskeyIdAndPassword -> {
            this.newAccount(instanceDomain)
        }
        is AccessToken.Pleroma -> {
            this.newAccount(instanceDomain)
        }
        is AccessToken.Firefish -> {
            this.newAccount(instanceDomain)
        }
    }
}



fun UserDTO.newAccount(instanceDomain: String, token: String): Account {
    return Account(
        remoteId = this.id,
        instanceDomain = instanceDomain,
        userName = this.userName,
        /*name = this.name,
        description = this.description,
        followersCount = this.followersCount?: 0,
        followingCount = this.followingCount?: 0,
        notesCount = this.notesCount?: 0,
        isBot = this.isBot,
        isCat = this.isCat,
        avatarUrl = this.avatarUrl,
        bannerUrl = this.bannerUrl,*/
        token = token,
        //emojis = this.emojis?: emptyList(),
        pages = emptyList(),
        instanceType = Account.InstanceType.MISSKEY
    )
}