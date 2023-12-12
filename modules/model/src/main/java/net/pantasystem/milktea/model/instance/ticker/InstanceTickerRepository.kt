package net.pantasystem.milktea.model.instance.ticker

interface InstanceTickerRepository {

    suspend fun sync(host: String): Result<Unit>

    suspend fun find(host: String): Result<InstanceTicker>

    suspend fun get(host: String): Result<InstanceTicker?>

    suspend fun findIn(hosts: List<String>): Result<List<InstanceTicker>>

}