package net.pantasystem.milktea.data.infrastructure.instance

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.milktea.CreateInstanceRequest
import net.pantasystem.milktea.api.milktea.InstanceInfoResponse
import net.pantasystem.milktea.api.milktea.MilkteaAPIServiceBuilder
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.instance.db.InstanceInfoDao
import net.pantasystem.milktea.data.infrastructure.instance.db.InstanceInfoRecord
import net.pantasystem.milktea.model.instance.InstanceInfo
import net.pantasystem.milktea.model.instance.InstanceInfoRepository
import javax.inject.Inject

class InstanceInfoRepositoryImpl @Inject constructor(
    private val instanceInfoDao: InstanceInfoDao,
    private val milkteaAPIServiceBuilder: MilkteaAPIServiceBuilder,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
): InstanceInfoRepository {
    private val milkteaAPIService by lazy {
        milkteaAPIServiceBuilder.build("https://milktea.pantasystem.net")
    }
    override suspend fun findAll(): Result<List<InstanceInfo>> = runCancellableCatching {
        withContext(ioDispatcher) {
            instanceInfoDao.findAll().map {
                it.toModel()
            }
        }
    }

    override suspend fun sync(): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val instances = requireNotNull(milkteaAPIService.getInstances().throwIfHasError().body())
            val models = instances.map {
                it.toModel()
            }
            instanceInfoDao.clear()
            instanceInfoDao.insertAll(models.map {
                it.toRecord()
            })
        }
    }

    override suspend fun findOne(id: String): Result<InstanceInfo> = runCancellableCatching {
        withContext(ioDispatcher) {
            instanceInfoDao.findById(id)?.toModel()
                ?: throw NoSuchElementException("指定されたId($id)のInstanceInfoは存在しません")
        }
    }

    override fun observeAll(): Flow<List<InstanceInfo>> {
        return instanceInfoDao.observeAll().map { list ->
            list.map {
                it.toModel()
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun findByHost(host: String): Result<InstanceInfo> = runCancellableCatching{
        withContext(ioDispatcher) {
            instanceInfoDao.findByHost(host)?.toModel() ?: throw NoSuchElementException()
        }
    }
    override fun observeByHost(host: String): Flow<InstanceInfo?> {
        return instanceInfoDao.observeByHost(host).map {
            it?.toModel()
        }
    }

    override suspend fun postInstance(host: String): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            milkteaAPIService.createInstance(CreateInstanceRequest(host = host)).throwIfHasError()
        }
    }

}

fun InstanceInfoRecord.toModel(): InstanceInfo {
    return InstanceInfo(
        id = id,
        host = host,
        name = name,
        description = description,
        clientMaxBodyByteSize = clientMaxBodyByteSize,
        iconUrl = iconUrl,
        themeColor = themeColor
    )
}

fun InstanceInfo.toRecord(): InstanceInfoRecord {
    return InstanceInfoRecord(
        id = id,
        host = host,
        name = name,
        description = description,
        clientMaxBodyByteSize = clientMaxBodyByteSize,
        iconUrl = iconUrl,
        themeColor = themeColor
    )
}

fun InstanceInfoResponse.toModel(): InstanceInfo {
    return InstanceInfo(
        id = id,
        host = host,
        name = name,
        description = description,
        clientMaxBodyByteSize = clientMaxBodyByteSize,
        iconUrl = iconUrl,
        themeColor = themeColor
    )
}