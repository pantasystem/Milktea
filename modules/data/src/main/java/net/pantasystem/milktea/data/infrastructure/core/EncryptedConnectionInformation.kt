package net.pantasystem.milktea.data.infrastructure.core

import androidx.room.Entity
import androidx.room.ForeignKey
import java.util.*

/**
 * @param accountId 接続する対象のUserId
 * @param instanceBaseUrl 接続する対象のインスタンスのURL
 * @param encryptedI encrypt済みのi
 * @param viaName appログイン出ない場合はnull
 */
@Suppress("DEPRECATION")
@Entity(
    tableName = "connection_information",
    foreignKeys = [ForeignKey(childColumns = ["accountId"], parentColumns = ["id"], entity = Account::class, onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.NO_ACTION)],
    primaryKeys = ["accountId", "encryptedI", "instanceBaseUrl"]
)
@Deprecated("model.accountへ移行")
data class EncryptedConnectionInformation(
    val accountId: String,
    val instanceBaseUrl: String,
    val encryptedI: String,
    val viaName: String?,
    val createdAt: Date = Date(),
    val isDirect: Boolean = false
){
    var updatedAt: Date = Date()


}