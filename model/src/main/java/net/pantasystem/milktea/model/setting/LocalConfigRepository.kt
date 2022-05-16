package net.pantasystem.milktea.model.setting

interface LocalConfigRepository {

    suspend fun save(config: Config): Result<Unit>
    suspend fun save(remember: RememberVisibility): Result<Unit>

    fun get(): Result<Config>
    fun getRememberVisibility(accountId: Long): Result<RememberVisibility>
}