package net.pantasystem.milktea.note.poll

import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.poll.Poll


data class OnVoted(
    val noteId: Note.Id,
    val choice: Poll.Choice,
    val poll: Poll,
)