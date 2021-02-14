package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import kotlinx.serialization.Serializable
import jp.panta.misskeyandroidclient.model.notification.Notification as NotificationDTO

@Serializable
data class EventMessage(
    val body: StreamingEvent
)

@Serializable
sealed class StreamingEvent

/**
 * bodyとして機能する
 */
sealed class ChannelEvent : StreamingEvent(){

    abstract val id: String

    @Serializable
    data class ReceiveNote(
        override val id: String,
        val body: NoteDTO
    ) : ChannelEvent()

    @Serializable
    sealed class Main : ChannelEvent(){

        @Serializable
        data class Notification(
            override val id: String,
            val body: NotificationDTO
        ) : Main()

        @Serializable
        data class ReadAllNotification(
            override val id: String,
        ) : Main()

        @Serializable
        data class UnreadMessagingMessage(
            override val id: String,
            val body: StreamingEvent
        ) : Main()

        @Serializable
        data class Mention(
            override val id: String,
            val body: NoteDTO
        ) : Main()

        @Serializable
        data class UnreadMention(
            override val id: String
        ) : Main()

        @Serializable
        data class MeUpdated(
            override val id: String,

            ) : Main()


    }
}

@Serializable
data class NoteUpdated (
    val id: String,
    val body: Body
) : StreamingEvent() {

    @Serializable
    sealed class Body {
        @Serializable
        class Reacted (
            val reaction: String,
            val userId: String,
        ) : Body()

        @Serializable
        class Unreacted (
            val reaction: String,
            val userId: String,
        ) : Body()

        @Serializable
        class PollVoted(
            val choice: Int,
            val userId: String
        ) : Body()

        @Serializable
        object Deleted : Body()
    }


}