package net.pantasystem.milktea.data.infrastructure.drive

import javax.inject.Inject
import javax.inject.Singleton
import net.pantasystem.milktea.common.runCancellableCatching

@Singleton
class ClearUnUsedDriveFileCacheJob @Inject constructor(
    private val driveFileRecordDao: DriveFileRecordDao
) {

    suspend fun checkAndClear(): Result<Unit> {
        return runCancellableCatching {
            driveFileRecordDao.deleteUnUsedFiles()
        }
    }
}