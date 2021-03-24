package jp.panta.misskeyandroidclient.model.core

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.ForeignKey.NO_ACTION
import androidx.room.Ignore
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.auth.AccessToken
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthBridge
import jp.panta.misskeyandroidclient.api.users.UserDTO
import java.lang.StringBuilder
import java.security.MessageDigest
import java.util.*

/**
 * @param accountId 接続する対象のUserId
 * @param instanceBaseUrl 接続する対象のインスタンスのURL
 * @param encryptedI encrypt済みのi
 * @param viaName appログイン出ない場合はnull
 */
@Entity(
    tableName = "connection_information",
    foreignKeys = [ForeignKey(childColumns = ["accountId"], parentColumns = ["id"], entity = Account::class, onDelete = CASCADE, onUpdate = NO_ACTION)],
    primaryKeys = ["accountId", "encryptedI", "instanceBaseUrl"]
)
data class EncryptedConnectionInformation(
    val accountId: String,
    val instanceBaseUrl: String,
    val encryptedI: String,
    val viaName: String?,
    val createdAt: Date = Date(),
    val isDirect: Boolean = false
){
    var updatedAt: Date = Date()

    @Ignore
    fun getI(encryption: Encryption): String?{
        return encryption.decrypt(accountId, encryptedI)
    }

    class Creator(val encryption: Encryption){


        fun create(
            accessToken: AccessToken,
            app: App,
            instanceDomain: String
        ): EncryptedConnectionInformation{
            val i = sha256(accessToken.accessToken + app.secret)
            return EncryptedConnectionInformation(
                accountId = accessToken.user.id,
                instanceBaseUrl = instanceDomain.adjustmentUrl(),
                encryptedI = encryption.encrypt(accessToken.user.id, i),
                viaName = app.name
            )
        }

        fun create(i: String, user: UserDTO, instanceDomain: String): EncryptedConnectionInformation{
            return EncryptedConnectionInformation(
                accountId = user.id,
                instanceBaseUrl = instanceDomain,
                encryptedI = encryption.encrypt(user.id, i),
                viaName = null,
                isDirect = true
            )
        }

        fun create(accessToken: AccessToken, customAuthBridge: CustomAuthBridge): EncryptedConnectionInformation{
            val i = sha256(accessToken.accessToken + customAuthBridge.secret)
            return EncryptedConnectionInformation(
                accountId = accessToken.user.id,
                instanceBaseUrl = customAuthBridge.instanceDomain,
                encryptedI = encryption.encrypt(accessToken.user.id, i),
                viaName = customAuthBridge.viaName
            )
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

        private fun String.adjustmentUrl(): String{
            val https = if(this.startsWith("https://")){
                this
            }else{
                "https://$this"
            }
            return if(https.endsWith("/")){
                this
            }else{
                "$this/"
            }
        }
    }

}