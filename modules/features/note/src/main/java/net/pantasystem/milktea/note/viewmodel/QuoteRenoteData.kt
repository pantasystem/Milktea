package net.pantasystem.milktea.note.viewmodel

import net.pantasystem.milktea.model.note.Note

data class QuoteRenoteData(
    val note: Note,
    val isRenoteToChannel: Boolean,
)