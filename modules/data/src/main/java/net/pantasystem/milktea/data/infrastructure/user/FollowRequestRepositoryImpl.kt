package net.pantasystem.milktea.data.infrastructure.user

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.users.AcceptFollowRequest
import net.pantasystem.milktea.api.misskey.users.RejectFollowRequest
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.FollowRequestRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class FollowRequestRepositoryImpl @Inject constructor(
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val userDataSource: UserDataSource,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
): FollowRequestRepository {

    override suspend fun accept(userId: User.Id): Boolean =
        withContext(ioDispatcher) {
            val account = accountRepository.get(userId.accountId).getOrThrow()
            val user = userRepository.find(userId, true) as User.Detail
            if (user.related?.hasPendingFollowRequestToYou != true) {
                return@withContext false
            }
            val res = misskeyAPIProvider.get(account)
                .acceptFollowRequest(
                    AcceptFollowRequest(
                        i = account.token,
                        userId = userId.id
                    )
                )
                .throwIfHasError()
            if (res.isSuccessful) {
                userDataSource.add(
                    user.copy(
                        related = user.related?.copy(
                            hasPendingFollowRequestToYou = false,
                            isFollower = true
                        )
                    )
                )
            }
            return@withContext res.isSuccessful

        }

    override suspend fun reject(userId: User.Id): Boolean =
        withContext(ioDispatcher) {
            val account = accountRepository.get(userId.accountId).getOrThrow()
            val user = userRepository.find(userId, true) as User.Detail
            if (user.related?.hasPendingFollowRequestToYou != true) {
                return@withContext false
            }
            val res = misskeyAPIProvider.get(account).rejectFollowRequest(
                RejectFollowRequest(
                    i = account.token,
                    userId = userId.id
                )
            )
                .throwIfHasError()
            if (res.isSuccessful) {
                userDataSource.add(
                    user.copy(
                        related = user.related?.copy(
                            hasPendingFollowRequestToYou = false,
                            isFollower = false
                        )
                    )
                )
            }
            return@withContext res.isSuccessful
        }
}