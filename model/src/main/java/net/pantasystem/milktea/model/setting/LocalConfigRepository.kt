package net.pantasystem.milktea.model.setting

interface LocalConfigRepository {

    suspend fun save(config: Config): Result<Unit>

    fun get(): Result<Config>
}