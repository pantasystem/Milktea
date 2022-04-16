package net.pantasystem.milktea.data.model.users.report

import net.pantasystem.milktea.data.model.notes.NoteRelation
import net.pantasystem.milktea.data.model.users.User

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