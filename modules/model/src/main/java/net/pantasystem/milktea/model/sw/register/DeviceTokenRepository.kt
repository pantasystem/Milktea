package net.pantasystem.milktea.model.sw.register

interface DeviceTokenRepository {

    suspend fun getOrCreate(): Result<String>

    fun save(deviceToken: String): Result<Unit>

    fun clear(): Result<Unit>

}