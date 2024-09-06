package net.pantasystem.milktea.data.infrastructure.note.reaction.impl

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import net.pantasystem.milktea.model.note.Note

@Entity(
    tableName = "reaction_authors",
    indices = [],
)
data class ReactionAuthorEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = "account_id") val accountId: Long,
    @ColumnInfo(name = "reaction") val reaction: String?,
    @ColumnInfo(name = "note_id") val noteId: String
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


@Entity(
    tableName = "reaction_authors_users",
    indices = [
        Index("reaction_author_id", "user_id"),
        Index("reaction_author_id")
    ],
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["reaction_author_id"],
            entity = ReactionAuthorEntity::class,
        )
    ],
    primaryKeys = [
        "reaction_author_id", "user_id"
    ]
)
data class ReactionUserEntity(
    @ColumnInfo("reaction_author_id") val reactionAuthorId: String,
    @ColumnInfo("user_id") val userId: String,
)

data class ReactionAuthorWithUsers(
    @Embedded
    val reaction: ReactionAuthorEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "reaction_author_id",
        entity = ReactionUserEntity::class,
    )
    val users: List<ReactionUserEntity>
)