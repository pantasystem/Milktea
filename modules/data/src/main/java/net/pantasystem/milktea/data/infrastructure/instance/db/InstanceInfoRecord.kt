package net.pantasystem.milktea.data.infrastructure.instance.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "instance_info_v1_table",
    indices = [Index("host", unique = true)]
)
data class InstanceInfoRecord(
    @PrimaryKey(autoGenerate = false) val id: String,
    val host: String,
    val name: String?,
    val description: String?,
    val clientMaxBodyByteSize: Long?,
    val iconUrl: String?,
    val themeColor: String?,
)
