package net.pantasystem.milktea.data.infrastructure.filter

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.infrastructure.filter.db.MastodonFilterDao
import net.pantasystem.milktea.data.infrastructure.filter.db.MastodonWordFilterRecord
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.filter.MastodonWordFilter
import net.pantasystem.milktea.model.filter.MastodonWordFilterRepository
import javax.inject.Inject

class MastodonWordFilterRepositoryImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val mastodonWordFilterCache: MastodonWordFilterCache,
    private val mastodonWordFilterDao: MastodonFilterDao,
    private val mastodonAPIProvider: MastodonAPIProvider,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : MastodonWordFilterRepository {
    override suspend fun sync(accountId: Long): Result<Unit> = runCancellableCatching{
        fetchAndUpdate(accountId)
    }

    override suspend fun findAll(accountId: Long): Result<List<MastodonWordFilter>> = runCancellableCatching{
        withContext(ioDispatcher) {
            val inCache = mastodonWordFilterCache.get(accountId)
            if (inCache != null) {
                return@withContext inCache
            }
            val inDb = mastodonWordFilterDao.findByAccount(accountId).map {
                it.toModel()
            }
            if (inDb.isNotEmpty()) {
                mastodonWordFilterCache.put(accountId, inDb)
                return@withContext inDb
            }
            fetchAndUpdate(accountId)
        }
    }

    override suspend fun observeAll(accountId: Long): Flow<List<MastodonWordFilter>> {
        return mastodonWordFilterDao.observeByAccount(accountId).map { list ->
            list.map {
                it.toModel()
            }
        }.flowOn(ioDispatcher).onEach {
            mastodonWordFilterCache.put(accountId, it)
        }
    }

    private suspend fun fetchAndUpdate(accountId: Long): List<MastodonWordFilter> {
        val account = accountRepository.get(accountId).getOrThrow()
        require(account.instanceType == Account.InstanceType.MASTODON) {
            "アカウントの種別はmastodonである必要性があります。account:$account"
        }
        val body = mastodonAPIProvider.get(account).getFilters()
            .throwIfHasError()
            .body()
        val remoteRes = requireNotNull(body).map {
            it.toModel(account)
        }
        mastodonWordFilterCache.put(accountId, remoteRes)
        mastodonWordFilterDao.deleteByAccount(accountId)
        mastodonWordFilterDao.insertAll(
            remoteRes.map {
                MastodonWordFilterRecord.from(it)
            }
        )
        return remoteRes
    }

}