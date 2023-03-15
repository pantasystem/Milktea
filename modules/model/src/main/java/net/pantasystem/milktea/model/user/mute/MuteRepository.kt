package net.pantasystem.milktea.model.user.mute

import net.pantasystem.milktea.model.user.User

interface MuteRepository {

    suspend fun create(createMute: CreateMute): Result<Unit>

    suspend fun delete(userId: User.Id): Result<Unit>

}