package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.note.reaction.impl.usercustom.ReactionUserSetting
import net.pantasystem.milktea.data.infrastructure.note.reaction.impl.usercustom.ReactionUserSettingDao
import net.pantasystem.milktea.model.emoji.UserEmojiConfig
import net.pantasystem.milktea.model.emoji.UserEmojiConfigRepository
import javax.inject.Inject

class UserEmojiConfigRepositoryImpl @Inject constructor(
    val reactionUserSettingDao: ReactionUserSettingDao,
    private val userEmojiConfigCache: UserEmojiConfigCache,
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
            userEmojiConfigCache.put(config.instanceDomain, findByInstanceDomain(instanceDomain = config.instanceDomain))
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
            val domains = configs.map {
                it.instanceDomain
            }.distinct()
            domains.map {
                userEmojiConfigCache.put(it, findByInstanceDomain(it))
            }
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
        }.also {
            userEmojiConfigCache.put(instanceDomain, it)
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
        val domains = settings.map {
            it.instanceDomain
        }.distinct()
        domains.map {
            userEmojiConfigCache.put(it, findByInstanceDomain(it))
        }
    }

    override suspend fun delete(setting: UserEmojiConfig): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            reactionUserSettingDao.delete(ReactionUserSetting(
                reaction = setting.reaction,
                instanceDomain = setting.instanceDomain,
                weight = setting.weight
            ))
        }
        userEmojiConfigCache.put(setting.instanceDomain, findByInstanceDomain(setting.instanceDomain))
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
        }.onEach {
            userEmojiConfigCache.put(instanceDomain, it)
        }.onStart {
            userEmojiConfigCache.get(instanceDomain)?.let {
                emit(it)
            }
        }.flowOn(ioDispatcher)
    }

}