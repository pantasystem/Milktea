package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.model.notification.Notification as NotificationDTO


data class EventMessage(
    val body: StreamingEvent
)

sealed class StreamingEvent

/**
 * bodyとして機能する
 */
sealed class ChannelEvent : StreamingEvent(){

    abstract val id: String

    data class ReceiveNote(
        override val id: String,
        val body: NoteDTO
    ) : ChannelEvent()


    sealed class Main : ChannelEvent(){

        data class Notification(
            override val id: String,
            val body: NotificationDTO
        ) : Main()

        data class ReadAllNotification(
            override val id: String,
        ) : Main()

        data class UnreadMessagingMessage(
            override val id: String,
            val body: StreamingEvent
        ) : Main()

        data class Mention(
            override val id: String,
            val body: NoteDTO
        ) : Main()

        data class UnreadMention(
            override val id: String
        ) : Main()

        data class MeUpdated(
            override val id: String,

            ) : Main()


    }
}

data class NoteUpdated (
    val id: String,
    val body: Body
) : StreamingEvent() {
    sealed class Body {
        class Reacted (
            val reaction: String,
            val userId: String,
        ) : Body()

        class Unreacted (
            val reaction: String,
            val userId: String,
        ) : Body()

        class PollVoted(
            val choice: Int,
            val userId: String
        ) : Body()

        object Deleted : Body()
    }


}