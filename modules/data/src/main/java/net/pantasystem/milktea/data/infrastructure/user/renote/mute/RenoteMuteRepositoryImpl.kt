package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMuteDTO
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteDao
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteRecord
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import net.pantasystem.milktea.model.user.renote.mute.RenoteMuteRepository
import javax.inject.Inject

class RenoteMuteRepositoryImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val renoteMuteDao: RenoteMuteDao,
    private val renoteMuteApiAdapter: RenoteMuteApiAdapter,
    private val isSupportRenoteMuteInstance: IsSupportRenoteMuteInstance,
    private val unPushedRenoteMutesDiffFilter: UnPushedRenoteMutesDiffFilter,
    private val cache: RenoteMuteCache,
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : RenoteMuteRepository {

    override suspend fun syncBy(accountId: Long): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            // NOTE: キャッシュを更新しておく
            findBy(accountId).getOrThrow()

            if (!isSupportRenoteMuteInstance(accountId)) {
                return@withContext
            }

            val account = accountRepository.get(accountId).getOrThrow()

            val mutes: List<RenoteMuteDTO> = FindAllRemoteRenoteMutes(
                account,
                renoteMuteApiAdapter
            ).invoke()

            val unPushedRenoteMutes = unPushedRenoteMutesDiffFilter(
                mutes,
                renoteMuteDao.findByUnPushed(account.accountId)
                    .map {
                        it.toModel()
                    }
            )


            coroutineScope {
                unPushedRenoteMutes.map {
                    async {
                        create(it.userId)
                    }
                }
            }.awaitAll()

            renoteMuteDao.deleteBy(account.accountId)
            cache.clearBy(account.accountId)

            val newModels = mutes.map {
                RenoteMute(
                    User.Id(accountId, it.id),
                    createdAt = it.createdAt,
                    postedAt = it.createdAt
                )
            }
            renoteMuteDao.insertAll(
                newModels.map {
                    RenoteMuteRecord.from(it)
                }
            )
            cache.addAll(newModels, true)

        }
    }

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
            val existing = findOne(userId).getOrNull()

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

    override suspend fun findOne(userId: User.Id): Result<RenoteMute> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            // NOTE: APIにIdを指定して取得する仕組みがないのでローカルのみから取得する
            val result = renoteMuteDao.findByUser(userId.accountId, userId.id)?.toModel()
            if (result == null) {
                cache.remove(userId)
            } else {
                cache.add(result)
            }
            result?: throw NoSuchElementException("RenoteMuteは存在しません:$userId")
        }
    }

    override suspend fun create(userId: User.Id): Result<RenoteMute> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            val account = accountRepository.get(userId.accountId).getOrThrow()
            val isNeedPush = when (val exists = findOne(userId).getOrNull()) {
                null -> {
                    renoteMuteDao.insert(
                        RenoteMuteRecord.from(
                            RenoteMute(
                                userId,
                                createdAt = Clock.System.now(),
                                postedAt = null,
                            )
                        )
                    )
                    true
                }
                else -> {
                    exists.postedAt == null
                }
            }
            val created = findOne(userId).getOrThrow()
            if (isNeedPush && isSupportRenoteMuteInstance(account.accountId)) {
                renoteMuteApiAdapter.create(
                    userId
                )
                // APIへの送信に成功したのでpostedAtに成功した日時を記録する
                renoteMuteDao.update(
                    RenoteMuteRecord.from(
                        created.copy(
                            postedAt = Clock.System.now()
                        )
                    )
                )
            }

            findOne(userId).getOrThrow()
        }
    }

    override suspend fun exists(userId: User.Id): Result<Boolean> = runCancellableCatching {
        (cache.exists(userId)
                && !cache.isNotFound(userId))
                || findOne(userId).getOrNull() != null
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

