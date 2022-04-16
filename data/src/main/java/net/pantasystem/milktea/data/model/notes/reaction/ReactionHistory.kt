package net.pantasystem.milktea.data.model.notes.reaction


import net.pantasystem.milktea.data.model.Entity
import net.pantasystem.milktea.data.model.EntityId
import net.pantasystem.milktea.data.model.notes.Note
import net.pantasystem.milktea.data.model.users.User
import java.util.*

data class ReactionHistory (
    val id: Id,
    val noteId: Note.Id,
    val createdAt: Date,
    val user: User,
    val type: String
) : Entity {

    data class Id(
        val reactionId: String,
        val accountId: Long
    ) : EntityId
}