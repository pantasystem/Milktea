package net.pantasystem.milktea.data.infrastructure.note.timeline.favorite

import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.common.MastodonLinkHeaderDecoder
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.note.NoteDataSourceAdder
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.note.timeline.favorite.FavoriteTimelineRepository
import net.pantasystem.milktea.model.note.timeline.favorite.FavoriteTimelineResponse
import javax.inject.Inject

class FavoriteTimelineRepositoryImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val noteDataSourceAdder: NoteDataSourceAdder,
): FavoriteTimelineRepository {
    override suspend fun findPreviousTimeline(
        accountId: Long,
        untilId: String?,
        untilDate: Long?,
        limit: Int
    ): Result<FavoriteTimelineResponse> = runCancellableCatching {
        val account = accountRepository.get(accountId).getOrThrow()
        when(account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                val res = misskeyAPIProvider.get(account).favorites(
                    NoteRequest(
                        i = account.token,
                        limit = limit,
                        untilId = untilId,
                        untilDate = untilDate
                    )
                ).throwIfHasError()
                val nextSinceId = res.body()?.firstOrNull()?.id
                val nextUntilId = res.body()?.lastOrNull()?.id
                val ids = noteDataSourceAdder.addNoteDtoListToDataSource(
                    account,
                    requireNotNull(res.body()).map { it.note }
                )
                FavoriteTimelineResponse(
                    timelineItems = ids,
                    sinceId = nextSinceId,
                    untilId = nextUntilId
                )
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                val res = mastodonAPIProvider.get(account).getFavouriteStatuses(
                    maxId = untilId,
                ).throwIfHasError()
                val decoder = MastodonLinkHeaderDecoder(res.headers()["Link"])
                val maxId = decoder.getMaxId()
                val minId = decoder.getMinId()
                val ids = noteDataSourceAdder.addTootStatusDtoListIntoDataSource(
                    account,
                    requireNotNull(res.body())
                )
                FavoriteTimelineResponse(
                    timelineItems = ids,
                    sinceId = maxId,
                    untilId = minId
                )
            }
        }
    }

    override suspend fun findLaterTimeline(
        accountId: Long,
        sinceId: String?,
        sinceDate: Long?,
        limit: Int
    ): Result<FavoriteTimelineResponse> = runCancellableCatching {
        val account = accountRepository.get(accountId).getOrThrow()
        when(account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                val res = misskeyAPIProvider.get(account).favorites(
                    NoteRequest(
                        i = account.token,
                        limit = limit,
                        sinceId = sinceId,
                        sinceDate = sinceDate
                    )
                ).throwIfHasError()
                val nextSinceId = res.body()?.firstOrNull()?.id
                val nextUntilId = res.body()?.lastOrNull()?.id
                val ids = noteDataSourceAdder.addNoteDtoListToDataSource(
                    account,
                    requireNotNull(res.body()).map { it.note }
                )
                FavoriteTimelineResponse(
                    timelineItems = ids,
                    sinceId = nextSinceId,
                    untilId = nextUntilId
                )
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                val res = mastodonAPIProvider.get(account).getFavouriteStatuses(
                    minId = sinceId,
                ).throwIfHasError()
                val decoder = MastodonLinkHeaderDecoder(res.headers()["Link"])
                val maxId = decoder.getMaxId()
                val minId = decoder.getMinId()
                val ids = noteDataSourceAdder.addTootStatusDtoListIntoDataSource(
                    account,
                    requireNotNull(res.body())
                )
                FavoriteTimelineResponse(
                    timelineItems = ids,
                    sinceId = maxId,
                    untilId = minId
                )
            }
        }
    }
}