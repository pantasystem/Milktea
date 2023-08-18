package net.pantasystem.milktea.model.note

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable

interface NoteStreaming {
    fun connect(getAccount: suspend ()-> Account, pageable: Pageable): Flow<Note>
}