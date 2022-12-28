package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.usercustom.ReactionUserSetting
import net.pantasystem.milktea.data.infrastructure.notes.reaction.impl.usercustom.ReactionUserSettingDao
import net.pantasystem.milktea.model.emoji.EmojiUserSetting
import net.pantasystem.milktea.model.emoji.UserEmojiConfigRepository
import javax.inject.Inject

class UserEmojiConfigRepositoryImpl @Inject constructor(
    val reactionUserSettingDao: ReactionUserSettingDao
): UserEmojiConfigRepository {
    override suspend fun saveAll(configs: List<EmojiUserSetting>): Result<Unit> = runCancellableCatching{
        withContext(Dispatchers.IO) {
            reactionUserSettingDao.insertAll(configs.map {
                ReactionUserSetting(
                    reaction = it.reaction,
                    instanceDomain = it.instanceDomain,
                    weight = it.weight
                )
            })
        }
    }

    override suspend fun findByInstanceDomain(instanceDomain: String): List<EmojiUserSetting> {
        return withContext(Dispatchers.IO) {
            reactionUserSettingDao.findByInstanceDomain(instanceDomain)?.map {
                EmojiUserSetting(
                    reaction = it.reaction,
                    instanceDomain = it.instanceDomain,
                    weight = it.weight
                )
            } ?: emptyList()
        }
    }

    override suspend fun deleteAll(settings: List<EmojiUserSetting>): Result<Unit> = runCancellableCatching {
        reactionUserSettingDao.deleteAll(settings.map {
            ReactionUserSetting(
                reaction = it.reaction,
                instanceDomain = it.instanceDomain,
                weight = it.weight
            )
        })
    }

    override suspend fun delete(setting: EmojiUserSetting): Result<Unit> = runCancellableCatching {
        withContext(Dispatchers.IO) {
            reactionUserSettingDao.delete(ReactionUserSetting(
                reaction = setting.reaction,
                instanceDomain = setting.instanceDomain,
                weight = setting.weight
            ))
        }
    }

    override fun observeByInstanceDomain(instanceDomain: String): Flow<List<EmojiUserSetting>> {
        return reactionUserSettingDao.observeByInstanceDomain(instanceDomain).map { settings ->
            settings.map {
                EmojiUserSetting(
                    reaction = it.reaction,
                    instanceDomain = it.instanceDomain,
                    weight = it.weight
                )
            }
        }.flowOn(Dispatchers.IO)
    }

}