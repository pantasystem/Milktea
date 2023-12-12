package net.pantasystem.milktea.data.infrastructure.instance.ticker

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.milktea.instance.ticker.InstanceTickerAPIServiceBuilder
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.instance.ticker.db.InstanceTickerDAO
import net.pantasystem.milktea.data.infrastructure.instance.ticker.db.InstanceTickerRecord
import net.pantasystem.milktea.model.instance.ticker.InstanceTicker
import net.pantasystem.milktea.model.instance.ticker.InstanceTickerRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

class InstanceTickerRepositoryImpl @Inject constructor(
    private val instanceTickerAPIServiceBuilder: InstanceTickerAPIServiceBuilder,
    private val instanceTickerDAO: InstanceTickerDAO,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : InstanceTickerRepository {

    private val instanceTickerAPIService by lazy {
        instanceTickerAPIServiceBuilder.build("https://milktea-instance-ticker.milktea.workers.dev/")
    }

    override suspend fun sync(host: String): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val response = instanceTickerAPIService.getInstanceInfo(host)
                .throwIfHasError()
            val instanceTickerNetworkDTO = response.body()!!
            val instanceTicker = instanceTickerNetworkDTO.toModel()
            instanceTickerDAO.insert(InstanceTickerRecord.fromModel(instanceTicker))
        }

    }

    override suspend fun find(host: String): Result<InstanceTicker> = runCancellableCatching {
        withContext(ioDispatcher) {
            val record = instanceTickerDAO.find(host)

            if (record != null && record.cachedAt > Clock.System.now().minus(7.days)) {
                record.toModel()
            } else {
                val response = instanceTickerAPIService.getInstanceInfo(host)
                    .throwIfHasError()
                val instanceTickerNetworkDTO = response.body()!!
                val instanceTicker = instanceTickerNetworkDTO.toModel()

                instanceTickerDAO.insert(InstanceTickerRecord.fromModel(instanceTicker))
                instanceTicker
            }
        }
    }

    override suspend fun findIn(hosts: List<String>): Result<List<InstanceTicker>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val records = instanceTickerDAO.findIn(hosts)

            val cachedRecords = records.filter { it.cachedAt > Clock.System.now().minus(7.days) }
            val cachedHosts = cachedRecords.map { it.uri }
            val uncachedHosts = hosts.filter { !cachedHosts.contains(it) }

            val uncachedRecords = uncachedHosts.map { host ->
                val response = instanceTickerAPIService.getInstanceInfo(host)
                    .throwIfHasError()
                val instanceTickerNetworkDTO = response.body()!!
                val instanceTicker = instanceTickerNetworkDTO.toModel()

                instanceTickerDAO.insert(InstanceTickerRecord.fromModel(instanceTicker))
                instanceTicker
            }

            cachedRecords.map { it.toModel() } + uncachedRecords
        }
    }
}