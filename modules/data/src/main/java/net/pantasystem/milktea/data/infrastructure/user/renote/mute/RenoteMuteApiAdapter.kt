package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import net.pantasystem.milktea.api.misskey.users.renote.mute.CreateRenoteMuteRequest
import net.pantasystem.milktea.api.misskey.users.renote.mute.DeleteRenoteMuteRequest
import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMuteDTO
import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMutesRequest
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

interface RenoteMuteApiAdapter {
    suspend fun create(userId: User.Id)
    suspend fun delete(userId: User.Id)
    suspend fun findBy(
        accountId: Long,
        sinceId: String? = null,
        untilId: String? = null,
    ): List<RenoteMuteDTO>
}

class RenoteMuteApiAdapterImpl @Inject constructor(
    val misskeyAPIProvider: MisskeyAPIProvider,
    val accountRepository: AccountRepository,
): RenoteMuteApiAdapter {

    override suspend fun create(userId: User.Id) {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        misskeyAPIProvider.get(account).createRenoteMute(
            CreateRenoteMuteRequest(
                i = account.token,
                userId = userId.id
            )
        ).throwIfHasError()
    }

    override suspend fun delete(userId: User.Id) {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        misskeyAPIProvider.get(account).deleteRenoteMute(
            DeleteRenoteMuteRequest(
                i = account.token,
                userId = userId.id
            )
        ).throwIfHasError()
    }

    override suspend fun findBy(
        accountId: Long,
        sinceId: String?,
        untilId: String?,
    ): List<RenoteMuteDTO> {
        val account = accountRepository.get(accountId).getOrThrow()
        return requireNotNull(
            misskeyAPIProvider.get(account).getRenoteMutes(
                RenoteMutesRequest(
                    i = account.token,
                    untilId = untilId,
                    sinceId = sinceId
                )
            ).throwIfHasError().body()
        )
    }


}