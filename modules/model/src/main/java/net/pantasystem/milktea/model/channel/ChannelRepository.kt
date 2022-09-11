package net.pantasystem.milktea.model.channel

interface ChannelRepository {


    suspend fun findOne(id: Channel.Id): Result<Channel>

    suspend fun create(model: CreateChannel): Result<Channel>


    suspend fun follow(id: Channel.Id): Result<Channel>

    suspend fun unFollow(id: Channel.Id): Result<Channel>

    suspend fun update(model: UpdateChannel): Result<Channel>

}