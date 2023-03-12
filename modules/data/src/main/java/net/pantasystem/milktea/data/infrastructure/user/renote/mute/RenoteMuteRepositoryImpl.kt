package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
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
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : RenoteMuteRepository {

    override suspend fun syncBy(accountId: Long): Result<Unit> = runCancellableCatching{
        withContext(coroutineDispatcher) {
            if (!isSupportRenoteMuteInstance(accountId)) {
                return@withContext
            }

            val account = accountRepository.get(accountId).getOrThrow()

            val mutes: List<RenoteMuteDTO> = FindAllRemoteRenoteMutes(
                account,
                renoteMuteApiAdapter
            ).invoke()

            val unPushedRenoteMutes = renoteMuteDao.findByUnPushed(account.accountId)
                .map {
                    it.toModel()
                }
                .filterNot { mute ->
                    mutes.any { dto ->
                        mute.userId.id == dto.muteeId
                    }
                }

            coroutineScope {
                unPushedRenoteMutes.map {
                    async {
                        create(it.userId)
                    }
                }
            }

            renoteMuteDao.deleteBy(account.accountId)
            renoteMuteDao.insertAll(
                mutes.map {
                    RenoteMute(
                        User.Id(accountId, it.id),
                        createdAt = it.createdAt,
                        postedAt = it.createdAt
                    )
                }.map {
                    RenoteMuteRecord.from(it)
                }
            )

        }
    }

    override suspend fun syncBy(userId: User.Id): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            if (!isSupportRenoteMuteInstance(userId.accountId)) {
                return@withContext
            }
        }
    }

    override suspend fun findBy(accountId: Long): Result<List<RenoteMute>> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            renoteMuteDao.findByAccount(accountId).map {
                it.toModel()
            }
        }
    }

    override suspend fun delete(userId: User.Id): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            val existing = findOne(userId).getOrNull()

            if (existing != null) {
                renoteMuteDao.delete(userId.accountId, userId.id)
            }

            try {
                renoteMuteApiAdapter.delete(userId)
            } catch (_: APIError.NotFoundException) {}
        }
    }

    override suspend fun findOne(userId: User.Id): Result<RenoteMute> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            // NOTE: APIにIdを指定して取得する仕組みがないのでローカルのみから取得する
            renoteMuteDao.findByUser(userId.accountId, userId.id)?.toModel()
                ?: throw NoSuchElementException("RenoteMuteは存在しません:$userId")
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
        findOne(userId).getOrNull() != null
    }

    override fun observeBy(accountId: Long): Flow<List<RenoteMute>> {
        return renoteMuteDao.observeBy(accountId).map { list ->
            list.map {
                it.toModel()
            }
        }
    }

}