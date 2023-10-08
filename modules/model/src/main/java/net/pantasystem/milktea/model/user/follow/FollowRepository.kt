package net.pantasystem.milktea.model.user.follow

import net.pantasystem.milktea.model.user.User

interface FollowRepository {
    suspend fun create(userId: User.Id): Result<Unit>

    suspend fun delete(userId: User.Id): Result<Unit>

    suspend fun update(userId: User.Id, params: FollowUpdateParams): Result<Unit>
}