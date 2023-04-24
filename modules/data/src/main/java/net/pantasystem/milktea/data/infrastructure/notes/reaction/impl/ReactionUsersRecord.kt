package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique
import net.pantasystem.milktea.model.notes.Note

@Entity
data class ReactionUsersRecord(
    @Id var id: Long = 0L,

    @Index var accountId: Long = 0L,

    @Index
    var noteId: String = "",

    @Unique
    @Index
    var accountIdAndNoteIdAndReaction: String = "",

    var reaction: String = "",

    var accountIds: MutableList<String> = mutableListOf()
) {

    companion object {
        fun generateUniqueId(noteId: Note.Id, reaction: String?): String {
            return if (reaction == null) {
                "${noteId.accountId}-${noteId.noteId}"
            } else {
                "${noteId.accountId}-${noteId.noteId}-$reaction"
            }

        }
    }
}