package net.pantasystem.milktea.data.infrastructure.drive

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.collection.LRUCache
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.drive.FilePropertyNotFoundException
import javax.inject.Inject

class MediatorFilePropertyDataSource @Inject constructor(
    private val driveFileRecordDao: DriveFileRecordDao,
    private val loggerFactory: Logger.Factory,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : FilePropertyDataSource {

    val logger by lazy {
        loggerFactory.create("MediatorFilePropertyDataSource")
    }

    val cache = LRUCache<FileProperty.Id, FileProperty>(25)


    override suspend fun add(
        fileProperty: FileProperty
    ): Result<AddResult> = runCancellableCatching {
        withContext(ioDispatcher) {
            cache.put(fileProperty.id, fileProperty)
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

            return@withContext AddResult.Canceled
        }
    }

    override suspend fun addAll(
        list: List<FileProperty>
    ): Result<List<AddResult>> = runCancellableCatching {
        withContext(ioDispatcher) {
            if (list.isEmpty()) {
                return@withContext emptyList()
            }
            list.forEach {
                cache.put(it.id, it)
            }
            val insertResults = driveFileRecordDao.insertAll(list.map {
                DriveFileRecord.from(it)
            })

            // NOTE: 既に作成されいているためInsertがキャンセルされたファイル
            val ignored = list.filterIndexed { index, _ ->
                insertResults[index] == -1L
            }

            logger.debug { "insert結果:$insertResults" }
            if (ignored.isEmpty()) {
                return@withContext list.map {
                    AddResult.Created
                }
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
                return@withContext insertResults.map {
                    if (it == -1L) AddResult.Canceled else AddResult.Created
                }
            }

            logger.debug { "必要更新件数:${needUpdates.size}" }
            // NOTE: 内容に変更があった場合更新をする
            needUpdates.map { record ->
                driveFileRecordDao.update(record.update(idAndFileMap.getValue(record.toFilePropertyId())))
            }

            return@withContext list.mapIndexed { index, fileProperty ->
                if (insertResults[index] != -1L) {
                    AddResult.Created
                } else if (needUpdates.any { it.equalFileProperty(fileProperty) }) {
                    AddResult.Updated
                } else {
                    AddResult.Canceled
                }
            }
        }
    }

    override suspend fun find(
        filePropertyId: FileProperty.Id
    ): Result<FileProperty> = runCancellableCatching {
        withContext(ioDispatcher) {
            var result = cache.get(filePropertyId)
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

    override suspend fun remove(
        fileProperty: FileProperty
    ): Result<Boolean> = runCancellableCatching {
        withContext(ioDispatcher) {
            try {
                driveFileRecordDao.delete(fileProperty.id.accountId, fileProperty.id.fileId)
                true
            } catch (e: Exception) {
                false
            }.also {
                cache.remove(fileProperty.id)
            }
        }
    }


    override fun observe(id: FileProperty.Id): Flow<FileProperty?> {
        return driveFileRecordDao.observe(id.accountId, id.fileId).distinctUntilChanged().map {
            it?.toFileProperty()
        }.flowOn(ioDispatcher)
    }

    override fun observeIn(ids: List<FileProperty.Id>): Flow<List<FileProperty>> {
        if (ids.isEmpty()) {
            return flowOf(emptyList())
        }
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
        }.distinctUntilChanged().onEach { list ->
            list.forEach {
                cache.put(it.id, it)
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun findIn(
        ids: List<FileProperty.Id>
    ): Result<List<FileProperty>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val inMemories = ids.mapNotNull {
                cache.get(it)
            }
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
        withContext(ioDispatcher) {
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