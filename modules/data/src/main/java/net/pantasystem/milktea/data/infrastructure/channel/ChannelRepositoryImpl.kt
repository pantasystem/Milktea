package net.pantasystem.milktea.data.infrastructure.channel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.channel.ChannelRepository
import net.pantasystem.milktea.model.channel.ChannelStateModel
import net.pantasystem.milktea.model.channel.CreateChannel
import net.pantasystem.milktea.model.channel.UpdateChannel
import javax.inject.Inject

class ChannelRepositoryImpl @Inject constructor(
    private val channelAPIAdapter: ChannelAPIAdapter,
    private val channelStateModel: ChannelStateModel,
    private val accountRepository: AccountRepository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : ChannelRepository {
    override suspend fun findOne(id: Channel.Id): Result<Channel> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                var channel = channelStateModel.get(id)
                if (channel == null) {
                    val account = accountRepository.get(id.accountId).getOrThrow()
                    channel = channelAPIAdapter.findOne(id).getOrThrow()
                        .toModel(account)
                    channelStateModel.add(channel)
                }
                channel
            }
        }
    }

    override suspend fun create(model: CreateChannel): Result<Channel> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                val account = accountRepository.get(model.accountId).getOrThrow()
                val channel = channelAPIAdapter.create(model).getOrThrow()
                    .toModel(account)
                channelStateModel.add(channel)
            }
        }
    }

    override suspend fun follow(id: Channel.Id): Result<Channel> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                var channel = findOne(id).getOrThrow()
                channelAPIAdapter.follow(id).getOrThrow()
                channel = channel.copy(isFollowing = true)
                channelStateModel.add(channel)
            }
        }
    }

    override suspend fun unFollow(id: Channel.Id): Result<Channel> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                var channel = findOne(id).getOrThrow()
                channelAPIAdapter.unFollow(id).getOrThrow()
                channel = channel.copy(isFollowing = false)
                channelStateModel.add(channel)
            }
        }
    }

    override suspend fun update(model: UpdateChannel): Result<Channel> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                val channel = channelAPIAdapter.update(model).getOrThrow()
                val account = accountRepository.get(model.id.accountId).getOrThrow()
                channelStateModel.add(channel.toModel(account))
            }
        }
    }

    override suspend fun findFollowedChannels(
        accountId: Long,
        sinceId: Channel.Id?,
        untilId: Channel.Id?,
        limit: Int
    ): Result<List<Channel>> = runCancellableCatching {
        withContext(ioDispatcher) {
            channelAPIAdapter.findFollowedChannels(accountId, sinceId, untilId, 99).mapCancellableCatching { list ->
                val account = accountRepository.get(accountId).getOrThrow()
                list.map {
                    it.toModel(account)
                }
            }.mapCancellableCatching {
                channelStateModel.addAll(it)
                it
            }.getOrThrow()

        }
    }
}