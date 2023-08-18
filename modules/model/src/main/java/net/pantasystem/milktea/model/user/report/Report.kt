package net.pantasystem.milktea.model.user.report

import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteRelation
import net.pantasystem.milktea.model.user.User

data class Report(
    val userId: User.Id,
    val noteIds: List<Note.Id>,
    val comment: String
)

fun NoteRelation.toReport(baseUrl: String) : Report {
    return Report(
        userId = this.user.id,
        noteIds = listOf(note.id),
        comment = when(this.note.type) {
            is Note.Type.Mastodon -> {
                ""
            }
            is Note.Type.Misskey -> {
                "$baseUrl/notes/${this.note.id.noteId}\n------"
            }
        }
    )
}