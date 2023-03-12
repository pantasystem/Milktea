package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import javax.inject.Inject

interface IsSupportRenoteMuteInstance {
    suspend operator fun invoke(accountId: Long): Boolean
}

class IsSupportRenoteMuteInstanceImpl @Inject constructor(): IsSupportRenoteMuteInstance {
    override suspend fun invoke(accountId: Long): Boolean {
        return false
    }
}