package net.pantasystem.milktea.model.user.block

import net.pantasystem.milktea.model.user.User

interface BlockRepository {

    suspend fun create(userId: User.Id): Result<Unit>

    suspend fun delete(userId: User.Id): Result<Unit>

}