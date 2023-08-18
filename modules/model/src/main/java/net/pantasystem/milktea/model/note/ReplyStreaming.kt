package net.pantasystem.milktea.model.note

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.account.Account

interface ReplyStreaming {
    fun connect(getAccount: suspend ()-> Account): Flow<Note>
}