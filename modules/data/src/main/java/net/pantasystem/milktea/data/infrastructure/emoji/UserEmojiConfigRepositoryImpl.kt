package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.usercustom.ReactionUserSetting
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.usercustom.ReactionUserSettingDao
import net.pantasystem.milktea.model.emoji.UserEmojiConfig
import net.pantasystem.milktea.model.emoji.UserEmojiConfigRepository
import javax.inject.Inject

class UserEmojiConfigRepositoryImpl @Inject constructor(
    val reactionUserSettingDao: ReactionUserSettingDao,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
): UserEmojiConfigRepository {

    override suspend fun save(config: UserEmojiConfig): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            reactionUserSettingDao.insert(
                ReactionUserSetting(
                    reaction = config.reaction,
                    instanceDomain = config.instanceDomain,
                    weight = config.weight
                )
            )
        }
    }

    override suspend fun saveAll(configs: List<UserEmojiConfig>): Result<Unit> = runCancellableCatching{
        withContext(ioDispatcher) {
            reactionUserSettingDao.insertAll(configs.map {
                ReactionUserSetting(
                    reaction = it.reaction,
                    instanceDomain = it.instanceDomain,
                    weight = it.weight
                )
            })
        }
    }

    override suspend fun findByInstanceDomain(instanceDomain: String): List<UserEmojiConfig> {
        return withContext(ioDispatcher) {
            reactionUserSettingDao.findByInstanceDomain(instanceDomain)?.map {
                UserEmojiConfig(
                    reaction = it.reaction,
                    instanceDomain = it.instanceDomain,
                    weight = it.weight
                )
            } ?: emptyList()
        }
    }

    override suspend fun deleteAll(settings: List<UserEmojiConfig>): Result<Unit> = runCancellableCatching {
        reactionUserSettingDao.deleteAll(settings.map {
            ReactionUserSetting(
                reaction = it.reaction,
                instanceDomain = it.instanceDomain,
                weight = it.weight
            )
        })
    }

    override suspend fun delete(setting: UserEmojiConfig): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            reactionUserSettingDao.delete(ReactionUserSetting(
                reaction = setting.reaction,
                instanceDomain = setting.instanceDomain,
                weight = setting.weight
            ))
        }
    }

    override fun observeByInstanceDomain(instanceDomain: String): Flow<List<UserEmojiConfig>> {
        return reactionUserSettingDao.observeByInstanceDomain(instanceDomain).map { settings ->
            settings.map {
                UserEmojiConfig(
                    reaction = it.reaction,
                    instanceDomain = it.instanceDomain,
                    weight = it.weight
                )
            }
        }.flowOn(ioDispatcher)
    }

}