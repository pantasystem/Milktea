package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMuteDTO
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteDao
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.delegate.CreateRenoteMuteAndPushToRemoteDelegate
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.delegate.FindRenoteMuteAndUpdateMemCacheDelegate
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.delegate.SyncRenoteMuteDelegate
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import net.pantasystem.milktea.model.user.renote.mute.RenoteMuteRepository
import javax.inject.Inject

internal class RenoteMuteRepositoryImpl @Inject constructor(
    private val renoteMuteDao: RenoteMuteDao,
    private val renoteMuteApiAdapter: RenoteMuteApiAdapter,
    private val isSupportRenoteMuteInstance: IsSupportRenoteMuteInstance,
    private val cache: RenoteMuteCache,
    private val findRenoteMuteAndUpdateMemCache: FindRenoteMuteAndUpdateMemCacheDelegate,
    private val createAndPushToRemote: CreateRenoteMuteAndPushToRemoteDelegate,
    private val syncRenoteMuteDelegate: SyncRenoteMuteDelegate,
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : RenoteMuteRepository {

    override suspend fun syncBy(accountId: Long): Result<Unit> = syncRenoteMuteDelegate(accountId)

    override suspend fun syncBy(userId: User.Id): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            if (!isSupportRenoteMuteInstance(userId.accountId)) {
                return@withContext
            }
        }
    }

    override suspend fun findBy(accountId: Long): Result<List<RenoteMute>> =
        runCancellableCatching {
            withContext(coroutineDispatcher) {
                renoteMuteDao.findByAccount(accountId).map {
                    it.toModel()
                }.also {
                    cache.clearBy(accountId)
                    cache.addAll(it, true)
                }
            }
        }

    override suspend fun delete(userId: User.Id): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            val existing = findRenoteMuteAndUpdateMemCache(userId).getOrNull()

            if (existing != null) {
                renoteMuteDao.delete(userId.accountId, userId.id)
                cache.remove(userId)
            }

            try {
                renoteMuteApiAdapter.delete(userId)
            } catch (_: APIError.NotFoundException) {
            }
        }
    }

    override suspend fun findOne(userId: User.Id): Result<RenoteMute> = findRenoteMuteAndUpdateMemCache(userId)

    override suspend fun create(userId: User.Id): Result<RenoteMute> = createAndPushToRemote(userId)

    override suspend fun exists(userId: User.Id): Result<Boolean> = runCancellableCatching {
        (cache.exists(userId)
                && !cache.isNotFound(userId))
                || findRenoteMuteAndUpdateMemCache(userId).getOrNull() != null
    }

    override fun observeBy(accountId: Long): Flow<List<RenoteMute>> {
        return renoteMuteDao.observeBy(accountId).map { list ->
            list.map {
                it.toModel()
            }
        }.onEach {
            cache.clearBy(accountId)
            cache.addAll(it, true)
        }.flowOn(coroutineDispatcher)
    }

    override fun observeOne(userId: User.Id): Flow<RenoteMute?> {
        return renoteMuteDao.observeByUser(userId.accountId, userId.id).map {
            it?.toModel()
        }.onEach {
            if (it == null) {
                cache.remove(userId)
            } else {
                cache.add(it)
            }
        }.flowOn(coroutineDispatcher)
    }

}

internal fun RenoteMuteDTO.toModel(accountId: Long): RenoteMute {
    return RenoteMute(
        User.Id(accountId, muteeId),
        createdAt = createdAt,
        postedAt = createdAt,
    )
}

