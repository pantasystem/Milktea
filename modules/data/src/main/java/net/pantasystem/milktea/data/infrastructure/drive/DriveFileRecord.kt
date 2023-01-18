package net.pantasystem.milktea.data.infrastructure.drive

import androidx.room.*
import kotlinx.datetime.Instant
import net.pantasystem.milktea.data.infrastructure.account.db.AccountRecord
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
            entity = AccountRecord::class,
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
    val createdAt: Instant?,
    val name: String,
    val type: String,
    val md5: String?,
    val size: Int?,
    val url: String,
    val isSensitive: Boolean,
    val thumbnailUrl: String?,
    val folderId: String?,
    val userId: String?,
    val comment: String?,
    val blurhash: String?,

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
            blurhash = blurhash,
            isSensitive = isSensitive,
        )
    }

    @Ignore
    fun toFilePropertyId(): FileProperty.Id {
        return FileProperty.Id(relatedAccountId, serverId)
    }

    @Ignore
    fun update(file: FileProperty): DriveFileRecord {
        require(toFilePropertyId() == file.id) {
            "同一Id外の更新を行うことはできません id:${toFilePropertyId()}, args id:${file.id}"
        }
        return copy(
            serverId = file.id.fileId,
            relatedAccountId = file.id.accountId,
            createdAt = file.createdAt,
            name = file.name,
            type = file.type,
            md5 = file.md5,
            size = file.size,
            url = file.url,
            isSensitive = file.isSensitive,
            thumbnailUrl = file.thumbnailUrl,
            folderId = file.folderId,
            userId = file.userId?.id,
            blurhash = file.blurhash,
            comment = file.comment
        )
    }

    @Ignore
    fun equalFileProperty(property: FileProperty): Boolean {
        return property.id != toFilePropertyId()
                || property.isSensitive != isSensitive
                || property.comment != comment
                || property.size != size
                || property.md5 != md5
                || property.name != name
                || property.thumbnailUrl != thumbnailUrl
                || property.url != url
                || property.userId?.id != userId
                || property.folderId != folderId
                || property.type != type
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
        blurhash = fileProperty.blurhash,
        id = 0L
    )
}