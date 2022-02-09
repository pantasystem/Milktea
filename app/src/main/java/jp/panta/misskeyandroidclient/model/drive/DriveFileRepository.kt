package jp.panta.misskeyandroidclient.model.drive

import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.drive.ShowFile
import jp.panta.misskeyandroidclient.api.drive.UpdateFileDTO
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

interface DriveFileRepository {
    suspend fun find(id: FileProperty.Id) : FileProperty
    suspend fun toggleNsfw(id: FileProperty.Id)
}

class DriveFileRepositoryImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val driveFileDataSource: FilePropertyDataSource,
    private val encryption: Encryption
) : DriveFileRepository{
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
}