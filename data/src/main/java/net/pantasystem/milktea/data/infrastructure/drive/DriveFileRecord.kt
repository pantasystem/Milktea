package net.pantasystem.milktea.data.infrastructure.drive

import androidx.room.*
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.user.User

@Entity(
    tableName = "drive_file_v1",
    indices = [
        Index("serverId", "relatedAccountId", unique = true),
        Index("serverId"),
        Index("relatedAccountId"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["accountId"],
            childColumns = ["relatedAccountId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DriveFileRecord(
    val serverId: String,
    val relatedAccountId: Long,
    val createdAt: Instant,
    val name: String,
    val type: String,
    val md5: String,
    val size: Int,
    val url: String,
    val isSensitive: Boolean,
    val thumbnailUrl: String?,
    val folderId: String?,
    val userId: String?,
    val comment: String?,

    @PrimaryKey(autoGenerate = true) val id: Long,
) {

    @Ignore
    fun toFileProperty(): FileProperty {
        return FileProperty(
            id = FileProperty.Id(relatedAccountId, serverId),
            createdAt = createdAt,
            md5 = md5,
            name = name,
            type = type,
            size = size,
            url = url,
            thumbnailUrl = thumbnailUrl,
            folderId = folderId,
            userId = userId?.let {
                User.Id(relatedAccountId, it)
            },
            comment = comment,
            isSensitive = isSensitive,
        )
    }

    companion object
}

fun DriveFileRecord.Companion.from(fileProperty: FileProperty): DriveFileRecord {

    return DriveFileRecord(
        serverId = fileProperty.id.fileId,
        createdAt = fileProperty.createdAt,
        md5 = fileProperty.md5,
        name = fileProperty.name,
        type = fileProperty.type,
        size = fileProperty.size,
        url = fileProperty.url,
        thumbnailUrl = fileProperty.thumbnailUrl,
        folderId = fileProperty.folderId,
        userId = fileProperty.userId?.id,
        comment = fileProperty.comment,
        isSensitive = fileProperty.isSensitive,
        relatedAccountId = fileProperty.id.accountId,
        id = 0L
    )
}