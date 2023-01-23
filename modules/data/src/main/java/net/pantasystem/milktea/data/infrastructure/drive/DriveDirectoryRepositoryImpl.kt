package net.pantasystem.milktea.data.infrastructure.drive

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.drive.CreateFolder
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.drive.CreateDirectory
import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.drive.DriveDirectoryRepository
import javax.inject.Inject

class DriveDirectoryRepositoryImpl @Inject constructor(
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : DriveDirectoryRepository {

    override suspend fun create(createDirectory: CreateDirectory): Result<Directory> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                val account = accountRepository.get(createDirectory.accountId).getOrThrow()
                val api = misskeyAPIProvider.get(account)
                api.createFolder(CreateFolder(
                    i = account.token,
                    name = createDirectory.directoryName,
                    parentId = createDirectory.parentId
                )).throwIfHasError().body()!!
            }
        }
    }
}