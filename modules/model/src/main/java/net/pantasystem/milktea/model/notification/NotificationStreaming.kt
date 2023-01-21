package net.pantasystem.milktea.model.notification

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.account.Account

interface NotificationStreaming {
    fun connect(getAccount: () -> Account): Flow<NotificationRelation>
}