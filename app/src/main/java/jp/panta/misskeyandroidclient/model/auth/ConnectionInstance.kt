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
    val accessToken: String
){
    fun getI(): String?{
        //return SecretConstant.i()
        val appSecret = SecretConstant.getInstances()[instanceBaseUrl]?: return null
        return sha256(accessToken + appSecret.appSecret)
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
}