package jp.panta.misskeyandroidclient.model.auth

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import jp.panta.misskeyandroidclient.SecretConstant
import jp.panta.misskeyandroidclient.model.Encryption
import java.lang.StringBuilder
import java.security.MessageDigest

@Entity(tableName = "connection_instance")
class ConnectionInstance(
    @PrimaryKey
    val userId: String,
    val instanceBaseUrl: String
){

    var encryptedCustomAppSecret: String? = null
    var encryptedI: String? = null
    var encryptedAccessToken: String? = null

    @Ignore
    fun getI(encryption: Encryption): String?{
        //return SecretConstant.i()
        val accessToken = getAccessToken(encryption)

        if(encryptedI != null){
            return getDirectI(encryption)
        }else if(encryptedCustomAppSecret != null && accessToken != null){
            val decrypted = getCustomAppSecret(encryption)
            return if(decrypted != null){
                return sha256(accessToken + decrypted)
            }else{
                null
            }
        }else{
            val appSecret = SecretConstant.getInstances()[instanceBaseUrl]?: return null
            return sha256(accessToken + appSecret.appSecret)
        }

    }

    @Ignore
    fun getDirectI(encryption: Encryption): String?{
        val i = encryptedI
        if(i != null){
            return encryption.decrypt(userId, i)
        }
        return null
    }

    @Ignore
    fun getCustomAppSecret(encryption: Encryption): String?{
        val secret = encryptedCustomAppSecret
        if(secret != null){
            encryption.decrypt(userId, secret)
        }
        return null
    }

    @Ignore
    fun setCustomAppSecret(secret: String, encryption: Encryption){
        encryptedCustomAppSecret = encryption.encrypt(userId, secret)
    }

    @Ignore
    fun setDirectI(i: String, encryption: Encryption){
        encryptedI = encryption.encrypt(userId, i)
    }

    @Ignore
    fun setAccessToken(token: String, encryption: Encryption){
        encryptedAccessToken = encryption.encrypt(userId, token)
    }

    @Ignore
    fun getAccessToken(encryption: Encryption): String?{
        val tmp = encryptedAccessToken
        if(tmp != null){
            return encryption.decrypt(userId, tmp)
        }
        return null
    }

    private fun sha256(input: String) = hashString("SHA-256", input)

    private fun hashString(type: String, input: String): String {
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }
        return result.toString()
    }

    override fun toString(): String {
        return "{userId:$userId, instanceBaseUrl:$instanceBaseUrl}"
    }
}