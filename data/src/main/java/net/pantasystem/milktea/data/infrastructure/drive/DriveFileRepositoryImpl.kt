package net.pantasystem.milktea.data.infrastructure.drive

import net.pantasystem.milktea.api.misskey.drive.DeleteFileDTO
import net.pantasystem.milktea.api.misskey.drive.ShowFile
import net.pantasystem.milktea.api.misskey.drive.UpdateFileDTO
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.infrastructure.toFileProperty
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.file.AppFile
import javax.inject.Inject


class DriveFileRepositoryImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val driveFileDataSource: FilePropertyDataSource,
    private val encryption: Encryption,
    private val driveFileUploaderProvider: FileUploaderProvider,
) : DriveFileRepository {
    override suspend fun find(id: FileProperty.Id): FileProperty {
        val file = runCatching {
            driveFileDataSource.find(id)
        }.getOrNull()
        if(file != null) {
            return file
        }
        val account = accountRepository.get(id.accountId)
        val api = misskeyAPIProvider.get(account.instanceDomain)
        val response = api.showFile(ShowFile(fileId = id.fileId, i = account.getI(encryption)))
            .throwIfHasError()
        val fp = response.body()!!.toFileProperty(account)
        driveFileDataSource.add(fp)
        return fp
    }

    override suspend fun toggleNsfw(id: FileProperty.Id) {
        val account = accountRepository.get(id.accountId)
        val api = misskeyAPIProvider.get(account.instanceDomain)
        val fileProperty = find(id)
        val result = api.updateFile(
            UpdateFileDTO(
                account.getI(encryption),
                fileId = id.fileId,
                isSensitive = !fileProperty.isSensitive,
                name = fileProperty.name,
                folderId = fileProperty.folderId,
                comment = fileProperty.comment
            )
        ).throwIfHasError()

        driveFileDataSource.add(result.body()!!.toFileProperty(account))
    }

    override suspend fun create(accountId:Long, file: AppFile.Local): Result<FileProperty> {
        return runCatching {
            val property = driveFileUploaderProvider.get(accountRepository.get(accountId))
                .upload(file, true)
                .toFileProperty(accountRepository.get(accountId))
            driveFileDataSource.add(property)
            driveFileDataSource.find(property.id)
        }
    }

    override suspend fun delete(id: FileProperty.Id): Result<Unit> {
        return runCatching {
            val account = accountRepository.get(id.accountId)
            val property = this.find(id)
            misskeyAPIProvider.get(account).deleteFile(
                DeleteFileDTO(i = account.getI(encryption), fileId = id.fileId)
            )
            driveFileDataSource.remove(property)
        }
    }
}