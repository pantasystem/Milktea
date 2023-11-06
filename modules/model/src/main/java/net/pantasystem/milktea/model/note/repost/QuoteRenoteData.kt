package net.pantasystem.milktea.model.note.repost

import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.note.Note

data class QuoteRenoteData(
    val noteId: Note.Id,
    val channelId: Channel.Id?,
) {
    companion object {
        fun ofTimeline(noteId: Note.Id): QuoteRenoteData {
            return QuoteRenoteData(
                noteId = noteId,
                channelId = null,
            )
        }

        fun ofChannel(noteId: Note.Id, channelId: Channel.Id): QuoteRenoteData {
            return QuoteRenoteData(
                noteId = noteId,
                channelId = channelId,
            )
        }
    }
}