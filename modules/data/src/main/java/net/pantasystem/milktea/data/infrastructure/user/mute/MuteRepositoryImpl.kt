package net.pantasystem.milktea.data.infrastructure.user.mute

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.toUserRelated
import net.pantasystem.milktea.data.infrastructure.user.UserActionResult
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.model.user.mute.CreateMute
import net.pantasystem.milktea.model.user.mute.MuteRepository
import javax.inject.Inject

internal class MuteRepositoryImpl @Inject constructor(
    private val muteApiAdapter: MuteApiAdapter,
    private val userDataSource: UserDataSource,
    private val userRepository: UserRepository,
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher,
    loggerFactory: Logger.Factory,
) : MuteRepository {

    private val logger by lazy {
        loggerFactory.create("MuteRepositoryImpl")
    }

    override suspend fun create(createMute: CreateMute): Result<Unit> {
        return runCancellableCatching {
            withContext(coroutineDispatcher) {
                updateCacheFrom(createMute.userId, muteApiAdapter.muteUser(createMute)) {
                    it.copy(
                        related = it.related?.copy(
                            isMuting = true
                        )
                    )
                }
            }
        }.onFailure {
            logger.error("ユーザーのミュートに失敗", it)
        }
    }

    override suspend fun delete(userId: User.Id): Result<Unit> {
        return runCancellableCatching {
            withContext(coroutineDispatcher) {
                updateCacheFrom(userId, muteApiAdapter.unmuteUser(userId)) {
                    it.copy(
                        related = it.related?.copy(
                            isMuting = false
                        )
                    )
                }
            }
        }.onFailure {
            logger.error("unmute failed", it)
        }
    }

    private suspend fun updateCacheFrom(userId: User.Id, result: UserActionResult, reducer: suspend (User.Detail) -> User.Detail) {
        val user = userRepository.find(userId, true) as User.Detail
        val updated = when(result) {
            is UserActionResult.Mastodon -> {
                user.copy(
                    related = result.relationship.toUserRelated()
                )
            }
            UserActionResult.Misskey -> {
                reducer(user)
            }
        }
        userDataSource.add(updated)
    }
}