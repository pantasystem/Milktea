package jp.panta.misskeyandroidclient.model.streming.note.v2

import jp.panta.misskeyandroidclient.model.emoji.Emoji


class NoteEvent(
    val noteId: String,
    val event: Event
)


sealed class Event{
    object Deleted : Event()

    class Voted(
        val choice: Int,
        val userId: String?
    ) : Event()

    class Reacted(
        val userId: String?,
        val reaction: String,
        val emoji: Emoji?
    ) : Event()

    class UnReacted(
        val userId: String?,
        val reaction: String
    ) : Event()
}
