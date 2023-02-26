package net.pantasystem.milktea.data.infrastructure.user

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.toUserRelated
import net.pantasystem.milktea.model.user.FollowRequestRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class FollowRequestRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val userDataSource: UserDataSource,
    private val followRequestApiAdapter: FollowRequestApiAdapter,
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
}