package net.pantasystem.milktea.common_navigation

import net.pantasystem.milktea.model.channel.Channel

interface ChannelNavigation : ActivityNavigation<ChannelNavigationArgs>
data class ChannelNavigationArgs(
    val specifiedAccountId: Long? = null,
    val addTabToAccountId: Long? = null,
)

interface ChannelDetailNavigation : ActivityNavigation<Channel.Id>