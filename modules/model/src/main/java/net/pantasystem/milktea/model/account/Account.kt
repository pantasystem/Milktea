package net.pantasystem.milktea.model.account


import androidx.room.*
import net.pantasystem.milktea.common.Encryption


import net.pantasystem.milktea.model.account.page.Page
import java.io.Serializable

@Entity(
    tableName = "account_table",
    indices = [
        Index("remoteId"),
        Index("instanceDomain"),
        Index("userName")
    ]
)
data class Account(
    val remoteId: String,
    val instanceDomain: String,
    val userName: String,
    val encryptedToken: String,
    @Ignore val pages: List<Page>,
    @ColumnInfo(name = "instanceType", defaultValue = "misskey") val instanceType: InstanceType,
    @PrimaryKey(autoGenerate = true) var accountId: Long = 0

) : Serializable {


    enum class InstanceType {
        MISSKEY, MASTODON
    }


    constructor(
        remoteId: String,
        instanceDomain: String,
        userName: String,
        instanceType: InstanceType,
        encryptedToken: String
    ) :
            this(
                remoteId,
                instanceDomain,
                userName,
                encryptedToken,
                emptyList(),
                instanceType
            )


    @Ignore
    private var decryptedI: String? = null

    fun getI(encryption: Encryption): String {
        return try {
            synchronized(this) {
                when (val i = decryptedI) {
                    null -> (encryption.decrypt(this.remoteId, this.encryptedToken)
                        ?: throw UnauthorizedException()).also { token ->
                        decryptedI = token
                    }
                    else -> i
                }
            }
        } catch (e: Exception) {
            throw UnauthorizedException(e.stackTraceToString())
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

class AccountInstanceTypeConverter {

    @TypeConverter
    fun convert(type: Account.InstanceType): String {
        // NOTE: enum.nameで取得するとリファクタリング時にデータが壊れる可能性があるのであえて愚直に変換している
        return when (type) {
            Account.InstanceType.MISSKEY -> "misskey"
            Account.InstanceType.MASTODON -> "mastodon"
        }
    }

    @TypeConverter
    fun convert(type: String): Account.InstanceType {
        // NOTE: enum.nameで取得するとリファクタリング時にデータが壊れる可能性があるのであえて愚直に変換している
        return when (type) {
            "misskey" -> Account.InstanceType.MISSKEY
            "mastodon" -> Account.InstanceType.MASTODON
            else -> throw IllegalArgumentException("未知のアカウント種別です")
        }
    }
}
