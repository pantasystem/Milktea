package net.pantasystem.milktea.data.infrastructure.instance.ticker

import net.pantasystem.milktea.api.milktea.instance.ticker.InstanceTickerAPIServiceBuilder
import net.pantasystem.milktea.model.instance.ticker.InstanceTicker
import net.pantasystem.milktea.model.instance.ticker.InstanceTickerRepository
import javax.inject.Inject

class InstanceTickerRepositoryImpl @Inject constructor(
    private val instanceTickerAPIServiceBuilder: InstanceTickerAPIServiceBuilder
): InstanceTickerRepository {

    private val instanceTickerAPIService by lazy {
        instanceTickerAPIServiceBuilder.build("https://milktea-instance-ticker.milktea.workers.dev/")
    }
    override suspend fun sync(host: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun find(host: String): Result<InstanceTicker> {
        TODO("Not yet implemented")
    }

    override suspend fun findIn(hosts: List<String>): Result<List<InstanceTicker>> {
        TODO("Not yet implemented")
    }
}