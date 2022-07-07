package net.pantasystem.milktea.data.infrastructure.drive

import net.pantasystem.milktea.api.misskey.drive.CreateFolder
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.drive.CreateDirectory
import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.drive.DriveDirectoryRepository
import javax.inject.Inject

class DriveDirectoryRepositoryImpl @Inject constructor(
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val encryption: Encryption
) : DriveDirectoryRepository {

    override suspend fun create(createDirectory: CreateDirectory): Result<Directory> {
        return runCatching {
            val account = accountRepository.get(createDirectory.accountId).getOrThrow()
            val api = misskeyAPIProvider.get(account)
            api.createFolder(CreateFolder(
                i = account.getI(encryption),
                name = createDirectory.directoryName,
                parentId = createDirectory.parentId
            )).throwIfHasError().body()!!
        }
    }
}