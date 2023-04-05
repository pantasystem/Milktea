package net.pantasystem.milktea.data.infrastructure.user.mute

import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.mastodon.accounts.MuteAccountRequest
import net.pantasystem.milktea.api.misskey.users.CreateMuteUserRequest
import net.pantasystem.milktea.api.misskey.users.RequestUser
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.user.UserActionResult
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.mute.CreateMute
import javax.inject.Inject

internal interface MuteApiAdapter {
    suspend fun muteUser(createMute: CreateMute): UserActionResult
    suspend fun unmuteUser(userId: User.Id): UnMuteResult
}

internal class MuteApiAdapterImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
) : MuteApiAdapter {

    override suspend fun muteUser(createMute: CreateMute): UserActionResult {
        val account = accountRepository.get(createMute.userId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                require(createMute.notifications == null) {
                    "Misskey does not support notifications mute account parameter"
                }
                misskeyAPIProvider.get(account).muteUser(
                    CreateMuteUserRequest(
                        i = account.token,
                        userId = createMute.userId.id,
                        expiresAt = createMute.expiresAt?.toEpochMilliseconds()
                    )
                ).throwIfHasError()
                UserActionResult.Misskey
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                val body = mastodonAPIProvider.get(account).muteAccount(
                    createMute.userId.id,
                    MuteAccountRequest(
                        duration = createMute.expiresAt?.let {
                            Clock.System.now().epochSeconds - it.epochSeconds
                        } ?: 0,
                        notifications = createMute.notifications ?: true
                    )
                ).throwIfHasError().body()
                UserActionResult.Mastodon(requireNotNull(body))
            }
        }
    }

    override suspend fun unmuteUser(userId: User.Id): UnMuteResult {
        val account = accountRepository.get(userId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account).unmuteUser(
                    RequestUser(
                        i = account.token,
                        userId = userId.id,
                    )
                ).throwIfHasError()
                UserActionResult.Misskey
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                val body = mastodonAPIProvider.get(account).unmuteAccount(userId.id)
                    .throwIfHasError()
                    .body()
                UserActionResult.Mastodon(requireNotNull(body))
            }
        }
    }
}

typealias UnMuteResult = UserActionResult