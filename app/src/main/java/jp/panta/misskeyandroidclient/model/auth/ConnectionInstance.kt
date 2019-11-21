package jp.panta.misskeyandroidclient.model.auth

import androidx.room.Entity
import androidx.room.PrimaryKey
import jp.panta.misskeyandroidclient.SecretConstant
import java.lang.StringBuilder
import java.security.MessageDigest

@Entity(tableName = "connection_instance")
data class ConnectionInstance(
    @PrimaryKey
    val userId: String,
    val instanceBaseUrl: String,
    val accessToken: String?,
    val directI: String? = null,
    val customAppSecret: String? = null

){
    fun getI(): String?{
        //return SecretConstant.i()
        if(directI != null){
            return directI
        }else if(customAppSecret != null && accessToken != null){
            return sha256(accessToken + customAppSecret)
        }else{
            val appSecret = SecretConstant.getInstances()[instanceBaseUrl]?: return null
            return sha256(accessToken + appSecret.appSecret)
        }

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