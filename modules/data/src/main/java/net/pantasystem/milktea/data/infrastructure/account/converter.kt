@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.data.infrastructure.account

import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.Hash
import net.pantasystem.milktea.data.infrastructure.auth.custom.AccessToken
import net.pantasystem.milktea.data.infrastructure.core.AccountRelation
import net.pantasystem.milktea.model.account.Account


fun AccessToken.Misskey.newAccount(instanceDomain: String, encryption: Encryption): Account {
    return this.user.newAccount(
        instanceDomain,
        encryption.encrypt(user.id , Hash.sha256(accessToken + appSecret))
    )
}
fun AccessToken.Mastodon.newAccount(
    instanceDomain: String,
    encryption: Encryption,
): Account {
    return Account(
        remoteId = this.account.id,
        userName = this.account.username,
        instanceDomain = instanceDomain,
        encryptedToken = encryption.encrypt(account.id, accessToken),
        instanceType = Account.InstanceType.MASTODON
    )
}


fun AccessToken.newAccount(instanceDomain: String, encryption: Encryption): Account {
    return when(this) {
        is AccessToken.Misskey -> {
            this.newAccount(instanceDomain, encryption)
        }
        is AccessToken.Mastodon -> {
            this.newAccount(instanceDomain, encryption)
        }
    }
}



fun UserDTO.newAccount(instanceDomain: String, encryptedToken: String): Account {
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
        encryptedToken = encryptedToken,
        //emojis = this.emojis?: emptyList(),
        pages = emptyList(),
        instanceType = Account.InstanceType.MISSKEY
    )
}
@Suppress("DEPRECATION")
fun AccountRelation.newAccount(user: UserDTO?): Account?{
    val ci = getCurrentConnectionInformation()
        ?: return null
    return user?.newAccount(ci.instanceBaseUrl, ci.encryptedI)
        ?: Account(
            remoteId = ci.accountId,
            instanceDomain = ci.instanceBaseUrl,
            userName = "",
            /*name = "",
            description = "",
            followersCount = 0,
            followingCount = 0,
            notesCount = 0,
            isBot = false,
            isCat = false,
            avatarUrl = null,
            bannerUrl = null,*/
            encryptedToken = ci.encryptedI,
//            pages = this.pages.mapNotNull{
//                it.toPage()
//            },
            instanceType = Account.InstanceType.MISSKEY,
        )
}

