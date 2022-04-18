package net.pantasystem.milktea.data.model.channel.impl

class ChannelRepositoryImpl(
    private val channelAPIAdapter: ChannelAPIAdapter,
    private val channelStateModel: net.pantasystem.milktea.model.channel.ChannelStateModel,
    private val accountRepository: net.pantasystem.milktea.model.account.AccountRepository
) : net.pantasystem.milktea.model.channel.ChannelRepository {
    override suspend fun findOne(id: net.pantasystem.milktea.model.channel.Channel.Id): Result<net.pantasystem.milktea.model.channel.Channel> {
        return runCatching {
            var channel = channelStateModel.get(id)
            if (channel == null) {
                val account = accountRepository.get(id.accountId)
                channel = channelAPIAdapter.findOne(id).getOrThrow()
                    .toModel(account)
                channelStateModel.add(channel)
            }
            return@runCatching channel
        }
    }

    override suspend fun create(model: net.pantasystem.milktea.model.channel.CreateChannel): Result<net.pantasystem.milktea.model.channel.Channel> {
        return runCatching {
            val account = accountRepository.get(model.accountId)
            val channel = channelAPIAdapter.create(model).getOrThrow()
                .toModel(account)
            channelStateModel.add(channel)
        }
    }

    override suspend fun follow(id: net.pantasystem.milktea.model.channel.Channel.Id): Result<net.pantasystem.milktea.model.channel.Channel> {
        return runCatching {
            var channel = findOne(id).getOrThrow()
            channelAPIAdapter.follow(id).getOrThrow()
            channel = channel.copy(isFollowing = true)
            channelStateModel.add(channel)
        }
    }

    override suspend fun unFollow(id: net.pantasystem.milktea.model.channel.Channel.Id): Result<net.pantasystem.milktea.model.channel.Channel> {
        return runCatching {
            var channel = findOne(id).getOrThrow()
            channelAPIAdapter.unFollow(id).getOrThrow()
            channel = channel.copy(isFollowing = false)
            channelStateModel.add(channel)
        }
    }

    override suspend fun update(model: net.pantasystem.milktea.model.channel.UpdateChannel): Result<net.pantasystem.milktea.model.channel.Channel> {
        return runCatching {
            val channel = channelAPIAdapter.update(model).getOrThrow()
            val account = accountRepository.get(model.id.accountId)
            channelStateModel.add(channel.toModel(account))
        }
    }
}