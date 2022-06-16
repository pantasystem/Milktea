package net.pantasystem.milktea.data.infrastructure.drive

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClearUnUsedDriveFileCacheJob @Inject constructor(
    private val driveFileRecordDao: DriveFileRecordDao
) {

    suspend fun checkAndClear(): Result<Unit> {
        return runCatching {
            driveFileRecordDao.deleteUnUsedFiles()
        }
    }
}