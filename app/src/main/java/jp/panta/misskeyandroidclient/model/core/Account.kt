package jp.panta.misskeyandroidclient.model.core

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * @param id 対となるUserのuserId
 */
@Entity
data class Account(
    @PrimaryKey(autoGenerate = false)
    val id: String
): Serializable