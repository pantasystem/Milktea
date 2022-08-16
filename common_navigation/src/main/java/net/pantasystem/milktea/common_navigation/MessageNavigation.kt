package net.pantasystem.milktea.common_navigation

import net.pantasystem.milktea.model.messaging.MessagingId

interface MessageNavigation :  ActivityNavigation<MessageNavigationArgs>

data class MessageNavigationArgs(
    val messagingId: MessagingId
)