package net.pantasystem.milktea.data.model.channel.impl

import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.api.misskey.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.v12.MisskeyAPIV12
import net.pantasystem.milktea.data.api.misskey.v12.channel.*
import net.pantasystem.milktea.data.model.Encryption
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.account.AccountRepository
import net.pantasystem.milktea.data.model.channel.Channel
import net.pantasystem.milktea.data.model.channel.ChannelAPIAdapter
import net.pantasystem.milktea.data.model.channel.CreateChannel
import net.pantasystem.milktea.data.model.channel.UpdateChannel
import javax.inject.Inject

class ChannelAPIAdapterWebImpl @Inject constructor(
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val encryption: Encryption
) : ChannelAPIAdapter {
    override suspend fun findOne(id: Channel.Id): Result<ChannelDTO> {
        return runCatching {
            val account = id.getAccount()
            id.getAPI().showChannel(
                ShowChannelDTO(
                i = account.getI(encryption),
                channelId = id.channelId
            )
            ).throwIfHasError().body()!!
        }

    }

    override suspend fun create(model: CreateChannel): Result<ChannelDTO> {
        return runCatching {
            val account = accountRepository.get(model.accountId)
            (misskeyAPIProvider.get(account) as MisskeyAPIV12).createChannel(
                CreateChannelDTO(
                    i = account.getI(encryption),
                    name = model.name,
                    description = model.description,
                    bannerId = model.bannerId
                )
            ).throwIfHasError().body()!!
        }
    }

    override suspend fun follow(id: Channel.Id): Result<Unit> {
        return runCatching {
            val account = id.getAccount()
            id.getAPI().followChannel(
                FollowChannelDTO(
                    i = account.getI(encryption),
                    channelId = id.channelId
                )
            ).throwIfHasError()
        }
    }

    override suspend fun unFollow(id: Channel.Id): Result<Unit> {
        return runCatching {
            val account = id.getAccount()
            id.getAPI().unFollowChannel(
                UnFollowChannelDTO(
                    i = account.getI(encryption),
                    channelId = id.channelId
                )
            ).throwIfHasError()
        }
    }

    override suspend fun update(model: UpdateChannel): Result<ChannelDTO> {
        return runCatching {
            val account = model.id.getAccount()
            model.id.getAPI()
                .updateChannel(
                    UpdateChannelDTO(
                        i = account.getI(encryption),
                        name = model.name,
                        description = model.description,
                        bannerId = model.bannerId
                    )
                ).throwIfHasError()
                .body()!!
        }
    }

    private suspend fun Channel.Id.getAPI(): MisskeyAPIV12 {
        val account = accountRepository.get(accountId)
        return misskeyAPIProvider.get(account) as MisskeyAPIV12
    }

    private suspend fun Channel.Id.getAccount(): Account {
        return accountRepository.get(accountId)
    }
}