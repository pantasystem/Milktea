package net.pantasystem.milktea.data.infrastructure.drive

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.drive.FilePropertyDataSourceState
import net.pantasystem.milktea.model.drive.FilePropertyNotFoundException
import javax.inject.Inject

class MediatorFilePropertyDataSource @Inject constructor(
    private val inMemoryFilePropertyDataSource: InMemoryFilePropertyDataSource,
    private val driveFileRecordDao: DriveFileRecordDao,
) : FilePropertyDataSource {


    override val state: StateFlow<FilePropertyDataSourceState>
        get() = inMemoryFilePropertyDataSource.state

    override suspend fun add(fileProperty: FileProperty): AddResult {
        val result = inMemoryFilePropertyDataSource.add(fileProperty)
        val record = runCatching {
            driveFileRecordDao.findOne(fileProperty.id.accountId, fileProperty.id.fileId)
        }.getOrNull()
        try {
            if (record == null) {
                driveFileRecordDao.insert(DriveFileRecord.from(fileProperty))
                return AddResult.CREATED
            } else if (record.toFileProperty() != fileProperty) {
                driveFileRecordDao.update(DriveFileRecord.from(fileProperty).copy(id = record.id))
                return AddResult.UPDATED
            }
        } catch (e: Exception) {
            return AddResult.CANCEL
        }

        return result
    }

    override suspend fun addAll(list: List<FileProperty>): List<AddResult> {
        return list.map {
            add(it)
        }
    }

    override suspend fun find(filePropertyId: FileProperty.Id): FileProperty {
        var result = runCatching {
            inMemoryFilePropertyDataSource.find(filePropertyId)
        }.getOrNull()
        if (result != null) {
            return result
        }
        result = runCatching {
            driveFileRecordDao.findOne(filePropertyId.accountId, filePropertyId.fileId)
                ?.toFileProperty()
        }.getOrNull()

        return result ?: throw FilePropertyNotFoundException(filePropertyId)
    }

    override suspend fun remove(fileProperty: FileProperty): Boolean {
        return try {
            driveFileRecordDao.delete(fileProperty.id.accountId, fileProperty.id.fileId)
            true
        } catch (e: Exception) {
            false
        } && inMemoryFilePropertyDataSource.remove(fileProperty)
    }


    override fun observe(id: FileProperty.Id): Flow<FileProperty?> {
        return driveFileRecordDao.observe(id.accountId, id.fileId).distinctUntilChanged().map {
            it?.toFileProperty()
        }
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

    override suspend fun findIn(ids: List<FileProperty.Id>): List<FileProperty> {
        val inMemories = inMemoryFilePropertyDataSource.findIn(ids)
        if (inMemories.size == ids.size) {
            return inMemories
        }
        val sets = inMemories.map { it.id }.toSet()
        val notExistsIds = ids.filterNot {
            sets.contains(it)
        }

        val onDb = notExistsIds.groupBy {
            it.accountId
        }.map { accountIdAndIds ->
            driveFileRecordDao.findIn(accountIdAndIds.key, accountIdAndIds.value.map { it.fileId })
        }.reduce { acc, list ->
            acc.toMutableList().also { mutable ->
                mutable.addAll(list)
            }
        }.map {
            it.toFileProperty()
        }
        val map = (inMemories + onDb).associateBy {
            it.id
        }
        return ids.mapNotNull {
            map[it]
        }
    }
}