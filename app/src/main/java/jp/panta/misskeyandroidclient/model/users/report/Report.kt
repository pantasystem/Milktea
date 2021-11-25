package jp.panta.misskeyandroidclient.model.users.report

import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.model.users.User

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