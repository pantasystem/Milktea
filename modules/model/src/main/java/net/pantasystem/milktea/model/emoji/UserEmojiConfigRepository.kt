package net.pantasystem.milktea.model.emoji

import kotlinx.coroutines.flow.Flow

interface UserEmojiConfigRepository {
    suspend fun saveAll(configs: List<UserEmojiConfig>): Result<Unit>
    suspend fun findByInstanceDomain(instanceDomain: String): List<UserEmojiConfig>
    suspend fun deleteAll(settings: List<UserEmojiConfig>): Result<Unit>
    suspend fun delete(setting: UserEmojiConfig): Result<Unit>
    fun observeByInstanceDomain(instanceDomain: String): Flow<List<UserEmojiConfig>>
}

