package jp.panta.misskeyandroidclient.model.channel

import jp.panta.misskeyandroidclient.api.misskey.v12.channel.ChannelDTO

interface ChannelAPIAdapter {

    suspend fun findOne(id: Channel.Id): Result<ChannelDTO>

    suspend fun create(model: CreateChannel): Result<ChannelDTO>


    suspend fun follow(id: Channel.Id): Result<Unit>

    suspend fun unFollow(id: Channel.Id): Result<Unit>

}