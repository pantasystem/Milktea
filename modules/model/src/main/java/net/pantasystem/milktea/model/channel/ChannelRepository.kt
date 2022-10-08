package net.pantasystem.milktea.model.channel

interface ChannelRepository {


    suspend fun findOne(id: Channel.Id): Result<Channel>

    suspend fun create(model: CreateChannel): Result<Channel>


    suspend fun follow(id: Channel.Id): Result<Channel>

    suspend fun unFollow(id: Channel.Id): Result<Channel>

    suspend fun update(model: UpdateChannel): Result<Channel>

    suspend fun findFollowedChannels(
        accountId: Long,
        sinceId: Channel.Id? = null,
        untilId: Channel.Id? = null,
        limit: Int = 99,
    ): Result<List<Channel>>
}