package net.pantasystem.milktea.model.user.reaction

import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteRelation
import net.pantasystem.milktea.model.user.User

data class UserReaction(
    val id: Id,
    val type: String,
    val noteId: Note.Id,
    val userId: User.Id,
    val createdAt: Instant,
) {
    data class Id(
        val accountId: Long,
        val serverId: String,
    )
}

data class UserReactionRelation(
    val reaction: UserReaction,
    val note: NoteRelation,
    val user: User,
)