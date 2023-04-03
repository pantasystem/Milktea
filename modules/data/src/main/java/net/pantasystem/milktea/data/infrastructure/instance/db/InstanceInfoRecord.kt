package net.pantasystem.milktea.data.infrastructure.instance.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "instance_info_v1_table",
    indices = [Index("host", unique = true)]
)
data class InstanceInfoRecord(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "host")
    val host: String,

    @ColumnInfo(name = "name")
    val name: String?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "clientMaxBodyByteSize")
    val clientMaxBodyByteSize: Long?,

    @ColumnInfo(name= "iconUrl")
    val iconUrl: String?,

    @ColumnInfo(name = "themeColor")
    val themeColor: String?,
)
