package jp.panta.misskeyandroidclient.model.account


import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.api.misskey.auth.AccessToken
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.api.misskey.users.UserDTO
import jp.panta.misskeyandroidclient.util.Hash
import java.io.Serializable

@Entity(
    tableName = "account_table",
    indices = [
        Index("remoteId"),
        Index("instanceDomain"),
        Index("userName")
    ]
)
data class Account (
    val remoteId: String,
    val instanceDomain: String,
    val userName: String,
    val encryptedToken: String,
    @Ignore val pages: List<Page>,
    @PrimaryKey(autoGenerate = true) var accountId: Long = 0

): Serializable{



    constructor(remoteId: String,
                instanceDomain: String,
                userName: String,
                encryptedToken: String) :
            this(
                remoteId,
                instanceDomain,
                userName,
                encryptedToken,
                emptyList()
            )



    fun getI(encryption: Encryption): String{
        return try{
            encryption.decrypt(this.remoteId, this.encryptedToken)
                ?: throw UnauthorizedException()
        }catch(e: Exception){
            throw UnauthorizedException()
        }
    }

    fun getHost(): String {
        if (instanceDomain.startsWith("https://")) {
            return instanceDomain.substring("https://".length, instanceDomain.length)
        } else if (instanceDomain.startsWith("http://")) {
            return instanceDomain.substring("http://".length, instanceDomain.length)
        }
        return instanceDomain
    }

}



fun AccessToken.newAccount(instanceDomain: String, encryption: Encryption, appSecret: String): Account{
    return this.user.newAccount(
        instanceDomain,
        encryption.encrypt(user.id , Hash.sha256(accessToken + appSecret))
    )
}

fun UserDTO.newAccount(instanceDomain: String, encryptedToken: String): Account{
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
        pages = emptyList()
    )
}
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
            pages = this.pages.mapNotNull{
                it.toPage()
            }
        )
}