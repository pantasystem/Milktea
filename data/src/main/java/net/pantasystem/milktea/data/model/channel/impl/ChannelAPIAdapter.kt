package net.pantasystem.milktea.data.model.channel.impl

import net.pantasystem.milktea.data.api.misskey.v12.channel.ChannelDTO
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.channel.CreateChannel
import net.pantasystem.milktea.model.channel.UpdateChannel

interface ChannelAPIAdapter {

    suspend fun findOne(id: Channel.Id): Result<ChannelDTO>

    suspend fun create(model: CreateChannel): Result<ChannelDTO>

    suspend fun follow(id: Channel.Id): Result<Unit>

    suspend fun unFollow(id: Channel.Id): Result<Unit>

    suspend fun update(model: UpdateChannel): Result<ChannelDTO>
}