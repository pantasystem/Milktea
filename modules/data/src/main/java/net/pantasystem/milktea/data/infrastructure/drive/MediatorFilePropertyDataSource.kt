package net.pantasystem.milktea.data.infrastructure.drive

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.drive.FilePropertyNotFoundException
import javax.inject.Inject

class MediatorFilePropertyDataSource @Inject constructor(
    private val inMemoryFilePropertyDataSource: InMemoryFilePropertyDataSource,
    private val driveFileRecordDao: DriveFileRecordDao,
    private val loggerFactory: Logger.Factory
) : FilePropertyDataSource {

    val logger by lazy {
        loggerFactory.create("MediatorFilePropertyDataSource")
    }


    override suspend fun add(fileProperty: FileProperty): Result<AddResult> = runCancellableCatching {
        withContext(Dispatchers.IO) {
            val result = inMemoryFilePropertyDataSource.add(fileProperty)
            val record = runCancellableCatching {
                driveFileRecordDao.findOne(fileProperty.id.accountId, fileProperty.id.fileId)
            }.getOrNull()
            try {
                if (record == null) {
                    driveFileRecordDao.insert(DriveFileRecord.from(fileProperty))
                    return@withContext AddResult.Created
                } else if (record.toFileProperty() != fileProperty) {
                    driveFileRecordDao.update(
                        DriveFileRecord.from(fileProperty).copy(id = record.id)
                    )
                    return@withContext AddResult.Updated
                }
            } catch (e: Exception) {
                return@withContext AddResult.Canceled
            }

            return@withContext result.getOrThrow()
        }
    }

    override suspend fun addAll(list: List<FileProperty>): Result<List<AddResult>> = runCancellableCatching {
        withContext(Dispatchers.IO) {
            if (list.isEmpty()) {
                return@withContext emptyList()
            }
            val results = inMemoryFilePropertyDataSource.addAll(list)
            val insertResults = driveFileRecordDao.insertAll(list.map {
                DriveFileRecord.from(it)
            })

            // NOTE: 既に作成されいているためInsertがキャンセルされたファイル
            val ignored = list.filterIndexed { index, _ ->
                insertResults[index] == -1L
            }

            logger.debug("insert結果:$insertResults")
            if (ignored.isEmpty()) {
                return@withContext results.getOrThrow()
            }

            // NOTE: DB上の同一のエンティティを取得する
            val records = findRecords(ignored.map { it.id })
            val idAndFileMap = ignored.associateBy { it.id }

            // NOTE: 内容に変更があったファイルをフィルタする
            val needUpdates = records.filter { record ->
                val property = idAndFileMap.getValue(record.toFilePropertyId())
                record.equalFileProperty(property)
            }

            if (needUpdates.isEmpty()) {
                return@withContext results.getOrThrow()
            }

            logger.debug("必要更新件数:${needUpdates.size}")
            // NOTE: 内容に変更があった場合更新をする
            needUpdates.map { record ->
                driveFileRecordDao.update(record.update(idAndFileMap.getValue(record.toFilePropertyId())))
            }

            return@withContext results.getOrThrow()
        }
    }

    override suspend fun find(filePropertyId: FileProperty.Id): Result<FileProperty> = runCancellableCatching {
        withContext(Dispatchers.IO) {
            var result = inMemoryFilePropertyDataSource.find(filePropertyId).getOrNull()
            if (result != null) {
                return@withContext result
            }
            result = runCancellableCatching {
                driveFileRecordDao.findOne(filePropertyId.accountId, filePropertyId.fileId)
                    ?.toFileProperty()
            }.getOrNull()

            result ?: throw FilePropertyNotFoundException(filePropertyId)
        }
    }

    override suspend fun remove(fileProperty: FileProperty): Result<Boolean> = runCancellableCatching {
        withContext(Dispatchers.IO) {
            try {
                driveFileRecordDao.delete(fileProperty.id.accountId, fileProperty.id.fileId)
                true
            } catch (e: Exception) {
                false
            } && inMemoryFilePropertyDataSource.remove(fileProperty).getOrThrow()
        }
    }


    override fun observe(id: FileProperty.Id): Flow<FileProperty?> {
        return driveFileRecordDao.observe(id.accountId, id.fileId).distinctUntilChanged().map {
            it?.toFileProperty()
        }.flowOn(Dispatchers.IO)
    }

    override fun observeIn(ids: List<FileProperty.Id>): Flow<List<FileProperty>> {
        val accountIds = ids.map { it.accountId }.distinct()
        val flows = accountIds.map { accountId ->
            driveFileRecordDao.observeIn(
                accountId,
                ids.filter { it.accountId == accountId }.map { it.fileId })
        }
        return combine(flows) { events ->
            val l1 = events.map { list ->
                list.map {
                    it.toFileProperty()
                }
            }
            l1.reduce { acc, list ->
                acc.toMutableList().also {
                    it.addAll(list)
                }
            }.associateBy {
                it.id
            }
        }.map { map ->
            ids.mapNotNull { id ->
                map[id]
            }
        }.distinctUntilChanged().onEach {
            inMemoryFilePropertyDataSource.addAll(it)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun findIn(ids: List<FileProperty.Id>): Result<List<FileProperty>> = runCancellableCatching {
        withContext(Dispatchers.IO) {
            val inMemories = inMemoryFilePropertyDataSource.findIn(ids).getOrThrow()
            if (inMemories.size == ids.size) {
                return@withContext inMemories
            }
            val sets = inMemories.map { it.id }.toSet()
            val notExistsIds = ids.filterNot {
                sets.contains(it)
            }

            val onDb = findRecords(notExistsIds).map {
                it.toFileProperty()
            }
            val map = (inMemories + onDb).associateBy {
                it.id
            }
            return@withContext ids.mapNotNull {
                map[it]
            }
        }
    }

    override suspend fun clearUnusedCaches(): Result<Unit> = runCancellableCatching {
        withContext(Dispatchers.IO) {
            driveFileRecordDao.deleteUnUsedFiles()
        }
    }

    private suspend fun findRecords(ids: List<FileProperty.Id>): List<DriveFileRecord> {

        val accountGroup = ids.groupBy {
            it.accountId
        }

        if (accountGroup.isEmpty()) {
            return emptyList()
        }

        val recordGroupedByAccount = accountGroup.map { accountIdAndIds ->
            driveFileRecordDao.findIn(accountIdAndIds.key, accountIdAndIds.value.map { it.fileId })
        }

        return recordGroupedByAccount.reduce { acc, list ->
            acc.toMutableList().also { mutable ->
                mutable.addAll(list)
            }
        }
    }
}