package net.pantasystem.milktea.model.file

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateAppFileSensitiveUseCase @Inject constructor(
    val driveFileRepository: DriveFileRepository,
    val filePropertyDataSource: FilePropertyDataSource,
    val accountRepository: AccountRepository,
) : UseCase {

    suspend operator fun invoke(appFile: AppFile, isSensitive: Boolean): Result<AppFile> = runCancellableCatching {
        when (appFile) {
            is AppFile.Local -> {
                appFile.copy(isSensitive = isSensitive)
            }
            is AppFile.Remote -> {
                val account = accountRepository.get(appFile.id.accountId).getOrThrow()
                when (account.instanceType) {
                    Account.InstanceType.MISSKEY -> {
                        val fileProperty = driveFileRepository.find(appFile.id)
                        driveFileRepository.update(
                            fileProperty.update(
                                isSensitive = isSensitive,
                            )
                        )
                        AppFile.Remote(fileProperty.id)
                    }
                    Account.InstanceType.MASTODON -> {
                        val fileProperty = filePropertyDataSource.find(appFile.id).getOrThrow()
                        filePropertyDataSource.add(
                            fileProperty.copy(
                                isSensitive = isSensitive,
                            )
                        )
                        AppFile.Remote(fileProperty.id)
                    }
                }
            }
        }
    }
}