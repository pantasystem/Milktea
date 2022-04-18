package net.pantasystem.milktea.model.user.report

import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.user.User

data class Report(
    val userId: User.Id,
    val comment: String
)

fun NoteRelation.toReport(baseUrl: String) : Report {
    return Report(
        userId = this.user.id,
        comment = "$baseUrl/notes/${this.note.id.noteId}\n------"
    )
}