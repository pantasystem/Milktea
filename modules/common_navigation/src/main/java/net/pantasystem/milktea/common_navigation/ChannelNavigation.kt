package net.pantasystem.milktea.common_navigation

import net.pantasystem.milktea.model.channel.Channel

interface ChannelNavigation : ActivityNavigation<Unit>

interface ChannelDetailNavigation : ActivityNavigation<Channel.Id>