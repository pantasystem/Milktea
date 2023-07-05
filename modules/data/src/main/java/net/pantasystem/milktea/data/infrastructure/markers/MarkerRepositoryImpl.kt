package net.pantasystem.milktea.data.infrastructure.markers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.mastodon.marker.SaveMarkersRequest
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.markers.MarkerRepository
import net.pantasystem.milktea.model.markers.MarkerType
import net.pantasystem.milktea.model.markers.Markers
import net.pantasystem.milktea.model.markers.SaveMarkerParams
import javax.inject.Inject

class MarkerRepositoryImpl @Inject constructor(
    val accountRepository: AccountRepository,
    val mastodonAPIProvider: MastodonAPIProvider,
    private val cache: MarkerCache,
    @IODispatcher val coroutineDispatcher: CoroutineDispatcher
) : MarkerRepository {

    override suspend fun find(accountId: Long, types: List<MarkerType>): Result<Markers> = runCancellableCatching{
        withContext(coroutineDispatcher) {
            when(val marker = cache.get(MarkerCache.Key(accountId, types))) {
                null -> Unit
                else -> return@withContext marker
            }
            val account = accountRepository.get(accountId).getOrThrow()
            when(account.instanceType) {
                Account.InstanceType.MISSKEY -> throw IllegalArgumentException("Not support markers feature when use misskey.")
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    val body = mastodonAPIProvider.get(account).getMarkers(types.map {
                        it.name.lowercase()
                    }).throwIfHasError().body()
                    val markers = Markers(
                        home = requireNotNull(body).home?.toModel(),
                        notifications = body.notifications?.toModel()
                    )
                    cache.put(MarkerCache.Key(accountId, types), markers)
                    markers
                }
            }
        }
    }

    override suspend fun save(accountId: Long, params: SaveMarkerParams): Result<Markers> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            val account = accountRepository.get(accountId).getOrThrow()
            when(account.instanceType) {
                Account.InstanceType.MISSKEY -> throw IllegalArgumentException("Not support markers feature when use misskey.")
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    val body = mastodonAPIProvider.get(account).saveMarkers(
                        markers = SaveMarkersRequest(
                            home = params.home?.let {
                                SaveMarkersRequest.SaveParams(it)
                            },
                            notifications = params.notifications?.let {
                                SaveMarkersRequest.SaveParams(it)
                            }
                        )
                    ).throwIfHasError().body()
                    val markers = Markers(
                        home = requireNotNull(body).home?.toModel(),
                        notifications = body.notifications?.toModel()
                    )
                    cache.put(MarkerCache.Key(accountId, listOfNotNull(
                        if (params.home == null) null else MarkerType.Home,
                        if (params.notifications == null) null else MarkerType.Notifications
                    )), markers)
                    markers
                }
            }
        }
    }

}