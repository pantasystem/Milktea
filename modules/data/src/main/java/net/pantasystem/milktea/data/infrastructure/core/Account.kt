package net.pantasystem.milktea.data.infrastructure.core

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * @param id 対となるUserのuserId
 */
@Deprecated("models.accountへ移行")
@Entity
data class Account(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo("id")
    val id: String
): Serializable