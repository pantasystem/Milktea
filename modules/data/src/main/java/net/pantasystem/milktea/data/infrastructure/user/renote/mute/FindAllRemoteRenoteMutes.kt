package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMuteDTO
import net.pantasystem.milktea.model.account.Account
import javax.inject.Inject

internal interface FindAllRemoteRenoteMutesDelegate {
    suspend operator fun invoke(account: Account): List<RenoteMuteDTO>
}
internal class FindAllRemoteRenoteMutesDelegateImpl @Inject constructor(
    private val renoteMuteApiAdapter: RenoteMuteApiAdapter,
) : FindAllRemoteRenoteMutesDelegate {

    override suspend operator fun invoke(account: Account): List<RenoteMuteDTO> {
        var mutes: List<RenoteMuteDTO> = emptyList()
        while (true) {
            val res = renoteMuteApiAdapter.findBy(
                account.accountId,
                untilId = mutes.lastOrNull()?.id
            )

            mutes = mutes + res
            if (res.isEmpty() || res.size < 11) {
                break
            }
        }
        return mutes
    }

}