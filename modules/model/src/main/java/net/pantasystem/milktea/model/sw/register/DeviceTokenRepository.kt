package net.pantasystem.milktea.model.sw.register

interface DeviceTokenRepository {

    suspend fun getOrCreate(): Result<String>

    suspend fun save(deviceToken: String): Result<Unit>

    suspend fun clear(): Result<Unit>

}