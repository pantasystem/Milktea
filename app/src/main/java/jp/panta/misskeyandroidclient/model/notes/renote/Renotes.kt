package jp.panta.misskeyandroidclient.model.notes.renote

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
