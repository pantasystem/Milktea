package net.pantasystem.milktea.data.infrastructure.channel

import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.api.misskey.v12.channel.ChannelDTO
import net.pantasystem.milktea.api.misskey.v12.channel.CreateChannelDTO
import net.pantasystem.milktea.api.misskey.v12.channel.FindPageable
import net.pantasystem.milktea.api.misskey.v12.channel.FollowChannelDTO
import net.pantasystem.milktea.api.misskey.v12.channel.ShowChannelDTO
import net.pantasystem.milktea.api.misskey.v12.channel.UnFollowChannelDTO
import net.pantasystem.milktea.api.misskey.v12.channel.UpdateChannelDTO
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.channel.CreateChannel
import net.pantasystem.milktea.model.channel.UpdateChannel
import javax.inject.Inject

class ChannelAPIAdapterWebImpl @Inject constructor(
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
) : ChannelAPIAdapter {
    override suspend fun findOne(id: Channel.Id): Result<ChannelDTO> {
        return runCancellableCatching {
            val account = id.getAccount()
            id.getAPI().showChannel(
                ShowChannelDTO(
                i = account.token,
                channelId = id.channelId
            )
            ).throwIfHasError().body()!!
        }

    }

    override suspend fun create(model: CreateChannel): Result<ChannelDTO> {
        return runCancellableCatching {
            val account = accountRepository.get(model.accountId).getOrThrow()
            (misskeyAPIProvider.get(account)).createChannel(
                CreateChannelDTO(
                    i = account.token,
                    name = model.name,
                    description = model.description,
                    bannerId = model.bannerId
                )
            ).throwIfHasError().body()!!
        }
    }

    override suspend fun follow(id: Channel.Id): Result<Unit> {
        return runCancellableCatching {
            val account = id.getAccount()
            id.getAPI().followChannel(
                FollowChannelDTO(
                    i = account.token,
                    channelId = id.channelId
                )
            ).throwIfHasError()
        }
    }

    override suspend fun unFollow(id: Channel.Id): Result<Unit> {
        return runCancellableCatching {
            val account = id.getAccount()
            id.getAPI().unFollowChannel(
                UnFollowChannelDTO(
                    i = account.token,
                    channelId = id.channelId
                )
            ).throwIfHasError()
        }
    }

    override suspend fun update(model: UpdateChannel): Result<ChannelDTO> {
        return runCancellableCatching {
            val account = model.id.getAccount()
            model.id.getAPI()
                .updateChannel(
                    UpdateChannelDTO(
                        i = account.token,
                        name = model.name,
                        description = model.description,
                        bannerId = model.bannerId
                    )
                ).throwIfHasError()
                .body()!!
        }
    }

    override suspend fun findFollowedChannels(
        accountId: Long,
        sinceId: Channel.Id?,
        untilId: Channel.Id?,
        limit: Int
    ): Result<List<ChannelDTO>> {
        return runCancellableCatching {
            val account = accountRepository.get(accountId).getOrThrow()
            val api = misskeyAPIProvider.get(account)
            api.followedChannels(FindPageable(
                i = account.token,
                sinceId = sinceId?.channelId,
                untilId = untilId?.channelId,
                limit = limit,
            )).throwIfHasError().body()!!
        }
    }
    private suspend fun Channel.Id.getAPI(): MisskeyAPI {
        val account = accountRepository.get(accountId).getOrThrow()
        return misskeyAPIProvider.get(account)
    }

    private suspend fun Channel.Id.getAccount(): Account {
        return accountRepository.get(accountId).getOrThrow()
    }
}