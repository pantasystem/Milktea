package jp.panta.misskeyandroidclient.model.channel

interface ChannelService {


    suspend fun findOne(id: Channel.Id): Result<Channel>

    suspend fun create(model: CreateChannel): Result<Channel>


    suspend fun follow(id: Channel.Id): Result<Channel>

    suspend fun unFollow(id: Channel.Id): Result<Channel>


}