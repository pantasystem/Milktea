package net.pantasystem.milktea.data.infrastructure.note.impl.db

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique
import io.objectbox.relation.ToMany

@Entity
data class ThreadRecord(
    @Id
    var id: Long = 0,


    var targetNoteId: String = "",
    var accountId: Long = 0L,

    @Index
    @Unique
    var targetNoteIdAndAccountId: String = "",
) {
    lateinit var ancestors: ToMany<NoteRecord>

    lateinit var descendants: ToMany<NoteRecord>
}