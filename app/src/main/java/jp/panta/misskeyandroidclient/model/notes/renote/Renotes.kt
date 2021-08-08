package jp.panta.misskeyandroidclient.model.notes.renote

import androidx.annotation.CheckResult
import jp.panta.misskeyandroidclient.model.notes.Note

sealed interface Renote {
    val noteId: Note.Id
    data class Quote(
        override val noteId: Note.Id
    ) : Renote

    data class Normal(
        override val noteId: Note.Id
    ) : Renote
}
data class Renotes (
    val renotes: List<Renote>,
    val targetNoteId: Note.Id
) {
    @CheckResult()
    fun addAllLast(list: List<Renote>) : Renotes{
        return this.copy(renotes = renotes.toMutableList().also{
            it.addAll(list)
        })
    }

    val quotes: List<Renote.Quote>
        get() = this.renotes.mapNotNull {
            it as? Renote.Quote
        }

    val onlyRenotes: List<Renote.Normal>
        get() = this.renotes.mapNotNull {
            it as? Renote.Normal
        }
}