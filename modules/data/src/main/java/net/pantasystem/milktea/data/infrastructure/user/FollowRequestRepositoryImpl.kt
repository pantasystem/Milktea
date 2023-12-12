package net.pantasystem.milktea.data.infrastructure.user

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.converters.MastodonAccountDTOEntityConverter
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.toUserRelated
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.*
import javax.inject.Inject

class FollowRequestRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val userDataSource: UserDataSource,
    private val followRequestApiAdapter: FollowRequestApiAdapter,
    private val accountRepository: AccountRepository,
    private val userDTOEntityConverter: UserDTOEntityConverter,
    private val mastodonAccountDTOEntityConverter: MastodonAccountDTOEntityConverter,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
): FollowRequestRepository {

    override suspend fun accept(userId: User.Id): Boolean =
        withContext(ioDispatcher) {
            val user = userRepository.find(userId, true) as User.Detail
            if (user.related?.hasPendingFollowRequestToYou != true) {
                return@withContext false
            }
            when(val result = followRequestApiAdapter.accept(userId)) {
                is FollowRequestResult.Mastodon -> {
                    userDataSource.add(
                        user.copy(
                            related = result.relationshipDTO.toUserRelated()
                        )
                    )
                }
                FollowRequestResult.Misskey -> {
                    userDataSource.add(
                        user.copy(
                            related = user.related?.copy(
                                hasPendingFollowRequestToYou = false,
                                isFollower = true
                            )
                        )
                    )
                }
            }
            true
        }

    override suspend fun reject(userId: User.Id): Boolean =
        withContext(ioDispatcher) {
            val user = userRepository.find(userId, true) as User.Detail
            if (user.related?.hasPendingFollowRequestToYou != true) {
                return@withContext false
            }
            when(val result = followRequestApiAdapter.reject(userId)) {
                is FollowRequestResult.Mastodon -> {
                    userDataSource.add(
                        user.copy(
                            related = result.relationshipDTO.toUserRelated()
                        )
                    )
                }
                FollowRequestResult.Misskey -> {
                    userDataSource.add(
                        user.copy(
                            related = user.related?.copy(
                                hasPendingFollowRequestToYou = false,
                                isFollower = false
                            )
                        )
                    )
                }
            }
            true
        }

    override suspend fun find(
        accountId: Long,
        sinceId: String?,
        untilId: String?
    ): FollowRequestsResult {
        return withContext(ioDispatcher) {
            val account = accountRepository.get(accountId).getOrThrow()
            when(val result = followRequestApiAdapter.findFollowRequests(
                accountId = accountId,
                sinceId = sinceId,
                untilId = untilId
            )) {
                is FindFollowRequestsResult.Mastodon -> {
                    val users = result.accounts.map {
                        mastodonAccountDTOEntityConverter.convert(account, it)
                    }
                    userDataSource.addAll(users)
                    FollowRequestsResult(
                        users = users,
                        sinceId = result.minId,
                        untilId = result.maxId,
                    )
                }
                is FindFollowRequestsResult.Misskey -> {
                    val first = result.userDTOs.firstOrNull()
                    val last = result.userDTOs.lastOrNull()
                    val users = result.userDTOs.map {
                        it.follower
                    }.distinctBy {
                        it.id
                    }.map {
                        userDTOEntityConverter.convert(account, it, false)
                    }
                    userDataSource.addAll(users)

                    FollowRequestsResult(
                        users = users,
                        sinceId = if (sinceId == null) first?.id else last?.id,
                        untilId = if (sinceId == null) last?.id else first?.id
                    )
                }
            }
        }
    }
}