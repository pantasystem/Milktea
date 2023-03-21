package net.pantasystem.milktea.data.converters

import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilePropertyDTOEntityConverter @Inject constructor() {

    suspend fun convert(filePropertyDTO: FilePropertyDTO, account: Account): FileProperty {
        return FileProperty(
            id = FileProperty.Id(account.accountId, filePropertyDTO.id),
            name = filePropertyDTO.name,
            createdAt = filePropertyDTO.createdAt,
            type = filePropertyDTO.type,
            md5 = filePropertyDTO.md5,
            size = filePropertyDTO.size ?: 0,
            userId = filePropertyDTO.userId?.let { User.Id(account.accountId, filePropertyDTO.userId!!) },
            folderId = filePropertyDTO.folderId,
            comment = filePropertyDTO.comment,
            isSensitive = filePropertyDTO.isSensitive ?: false,
            url = filePropertyDTO.getUrl(account.normalizedInstanceUri),
            thumbnailUrl = filePropertyDTO.getThumbnailUrl(account.normalizedInstanceUri),
            blurhash = filePropertyDTO.blurhash,
            properties = filePropertyDTO.properties?.let {
                FileProperty.Properties(it.width, it.height)
            }
        )
    }
}