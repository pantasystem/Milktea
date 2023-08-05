package net.pantasystem.milktea.data.infrastructure.account.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
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
data class AccountRecord(
    @ColumnInfo(name = "remoteId")
    val remoteId: String,

    @ColumnInfo(name = "instanceDomain")
    val instanceDomain: String,

    @ColumnInfo(name = "userName")
    val userName: String,

    @ColumnInfo(name = "encryptedToken")
    val encryptedToken: String,

    @Ignore val pages: List<Page>,
    @ColumnInfo(name = "instanceType", defaultValue = "misskey")
    val instanceType: Account.InstanceType,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "accountId")
    var accountId: Long = 0
) : Serializable {



    companion object {
        @Ignore
        fun from(account: Account, encryption: Encryption): AccountRecord {
            val encryptedToken = encryption.encrypt(account.remoteId, account.token)
            return AccountRecord(
                remoteId = account.remoteId,
                instanceDomain = account.instanceDomain,
                userName = account.userName,
                encryptedToken = encryptedToken,
                pages = account.pages,
                instanceType = account.instanceType,
                accountId = account.accountId
            )
        }
    }

    @Ignore
    fun toAccount(encryption: Encryption): Account {
        val decryptedToken = runCancellableCatching {
            requireNotNull(encryption.decrypt(remoteId, encryptedToken))
        }.getOrElse {
            ""
        }

        return Account(
            remoteId = remoteId,
            instanceDomain = instanceDomain,
            userName = userName,
            token = decryptedToken,
            pages = pages,
            instanceType = instanceType,
            accountId = accountId,
        )
    }

    constructor(
        remoteId: String,
        instanceDomain: String,
        userName: String,
        instanceType: Account.InstanceType,
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
            Account.InstanceType.PLEROMA -> "pleroma"
            Account.InstanceType.FIREFISH -> "firefish"
        }
    }

    @TypeConverter
    fun convert(type: String): Account.InstanceType {
        // NOTE: enum.nameで取得するとリファクタリング時にデータが壊れる可能性があるのであえて愚直に変換している
        return when (type) {
            "misskey" -> Account.InstanceType.MISSKEY
            "mastodon" -> Account.InstanceType.MASTODON
            "pleroma" -> Account.InstanceType.PLEROMA
            "firefish" -> Account.InstanceType.FIREFISH
            else -> throw IllegalArgumentException("未知のアカウント種別です")
        }
    }
}
