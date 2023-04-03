package net.pantasystem.milktea.data.infrastructure.user

import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountRelationshipDTO
import net.pantasystem.milktea.data.infrastructure.toUserRelated
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject


sealed interface UserActionResult {
    object Misskey : UserActionResult
    data class Mastodon(val relationship: MastodonAccountRelationshipDTO) : UserActionResult
}

internal interface UserCacheUpdaterFromUserActionResult {
    suspend operator fun invoke(
        userId: User.Id,
        result: UserActionResult,
        reducer: suspend (User.Detail) -> User.Detail,
    )
}

internal class UserCacheUpdaterFromUserActionResultImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val userDataSource: UserDataSource,
) : UserCacheUpdaterFromUserActionResult {
    override suspend fun invoke(
        userId: User.Id,
        result: UserActionResult,
        reducer: suspend (User.Detail) -> User.Detail,
    ) {
        val user = userRepository.find(userId, true) as User.Detail
        val updated = when (result) {
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