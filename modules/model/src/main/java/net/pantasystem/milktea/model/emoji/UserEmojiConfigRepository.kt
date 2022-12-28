package net.pantasystem.milktea.model.emoji

import kotlinx.coroutines.flow.Flow

interface UserEmojiConfigRepository {
    suspend fun saveAll(configs: List<EmojiUserSetting>): Result<Unit>
    suspend fun findByInstanceDomain(instanceDomain: String): List<EmojiUserSetting>
    suspend fun deleteAll(settings: List<EmojiUserSetting>): Result<Unit>
    suspend fun delete(setting: EmojiUserSetting): Result<Unit>
    fun observeByInstanceDomain(instanceDomain: String): Flow<List<EmojiUserSetting>>
}

