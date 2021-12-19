package jp.panta.misskeyandroidclient.model.drive

import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.drive.ShowFile
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.AccountRepository

interface DriveFileRepository {
    suspend fun find(id: FileProperty.Id) : FileProperty
}

class DriveFileRepositoryImpl(
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
}