package net.pantasystem.milktea.data.infrastructure.hashtag

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.EmptyRequest
import net.pantasystem.milktea.api.misskey.hashtag.SearchHashtagRequest
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.hashtag.Hashtag
import net.pantasystem.milktea.model.hashtag.HashtagRepository
import javax.inject.Inject

class HashtagRepositoryImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : HashtagRepository {
    override suspend fun search(
        accountId: Long,
        query: String,
        limit: Int,
        offset: Int,
    ): Result<List<String>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = accountRepository.get(accountId).getOrThrow()
            when (account.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    misskeyAPIProvider.get(account).searchHashtag(
                        SearchHashtagRequest(
                            query = query,
                            limit = limit,
                            offset = offset
                        )
                    ).throwIfHasError().body() ?: emptyList()
                }
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    mastodonAPIProvider.get(account).search(
                        q = query,
                        limit = limit,
                        offset = offset,
                        type = "hashtags"
                    ).throwIfHasError().body()?.hashtags?.map {
                        it.name
                    } ?: emptyList()
                }
            }

        }
    }

    override suspend fun trends(accountId: Long): Result<List<Hashtag>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = accountRepository.get(accountId).getOrThrow()
            when(account.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    val body = requireNotNull(
                        misskeyAPIProvider.get(account).getTrendingHashtags(EmptyRequest)
                            .throwIfHasError()
                            .body()
                    )
                    body.map {
                        it.toModel()
                    }
                }
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    val body = requireNotNull(
                        mastodonAPIProvider.get(account).getTagTrends()
                            .throwIfHasError()
                            .body()
                    )
                    body.map {
                        it.toModel()
                    }
                }
            }
        }

    }
}