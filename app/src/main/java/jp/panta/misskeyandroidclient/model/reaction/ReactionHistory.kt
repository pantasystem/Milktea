package jp.panta.misskeyandroidclient.model.reaction

import jp.panta.misskeyandroidclient.model.Entity
import jp.panta.misskeyandroidclient.model.EntityId
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.users.User
import java.util.*

data class ReactionHistory (
    val id: Id,
    val noteId: Note.Id,
    val createdAt: Date,
    val user: User,
    val type: String
) : Entity{

    data class Id(
        val reactionId: String,
        val accountId: Long
    ) : EntityId
}