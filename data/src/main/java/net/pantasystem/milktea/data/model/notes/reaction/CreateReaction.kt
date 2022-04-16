package net.pantasystem.milktea.data.model.notes.reaction

import jp.panta.misskeyandroidclient.model.notes.Note

data class CreateReaction(
    val noteId: Note.Id,
    val reaction: String
)