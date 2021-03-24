package jp.panta.misskeyandroidclient.model.account


import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.auth.AccessToken
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.model.UnauthorizedException
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
    /*val name: String?,
    val description: String?,
    val followersCount: Int,
    val followingCount: Int,
    val notesCount: Int,
    val isBot: Boolean,
    val isCat: Boolean,
    val avatarUrl: String?,
    val bannerUrl: String?,*/
    val encryptedToken: String,
    //@Ignore val emojis: List<Emoji>,

    @Ignore val pages: List<Page>,
    @PrimaryKey(autoGenerate = true) var accountId: Long = 0

): Serializable{



    constructor(remoteId: String,
                instanceDomain: String,
                userName: String,
                /*name: String?,
                description: String?,
                followersCount: Int,
                followingCount: Int,
                notesCount: Int,
                isBot: Boolean,
                isCat: Boolean,
                avatarUrl: String?,
                bannerUrl: String?,*/
                encryptedToken: String) :
            this(
                remoteId,
                instanceDomain,
                userName,
                /*name,
                description,
                followersCount,
                followingCount,
                notesCount,
                isBot,
                isCat,
                avatarUrl,
                bannerUrl,*/
                encryptedToken,
                //emptyList(),
                emptyList()
            )

    /*fun update(user: User): Account{
        if(user.id != this.remoteId){
            return this
        }
        return this.copy(
            name = user.name,
            description = user.description,
            followingCount = user.followingCount?: this.followingCount,
            followersCount = user.followersCount?: this.followersCount,
            notesCount = user.notesCount?: this.notesCount,
            isBot = user.isBot,
            isCat = user.isCat,
            avatarUrl = user.avatarUrl,
            bannerUrl = user.bannerUrl,
            emojis = user.emojis?: emptyList()
        )
    }*/

    fun getI(encryption: Encryption): String{
        return try{
            encryption.decrypt(this.remoteId, this.encryptedToken)
                ?: throw UnauthorizedException()
        }catch(e: Exception){
            throw UnauthorizedException()
        }
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

fun UserDTO.newAccount(instanceDomain: String, encryption: Encryption, token: String): Account{
    return newAccount(instanceDomain, encryption.encrypt(this.id, token))
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