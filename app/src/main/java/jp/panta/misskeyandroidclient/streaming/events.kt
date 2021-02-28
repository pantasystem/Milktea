package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import java.util.*
import jp.panta.misskeyandroidclient.model.notification.Notification as NotificationDTO

@Serializable
data class EventMessage(
    val body: StreamingEvent
)

@Serializable
sealed class StreamingEvent

/*data class ChannelEvent : StreamingEvent() {

}*/

@Serializable
@SerialName("channel")
data class ChannelEvent(
    val body: ChannelBody
) : StreamingEvent()

@Serializable
sealed class ChannelBody : StreamingEvent(){

    abstract val id: String

    @Serializable
    @SerialName("note")
    data class ReceiveNote(
        override val id: String,
        val body: NoteDTO
    ) : ChannelBody()

    @Serializable
    sealed class Main : ChannelBody(){

        @Serializable
        @SerialName("notification")
        data class Notification(
            override val id: String,
            val body: NotificationDTO
        ) : Main()

        @Serializable
        @SerialName("readAllNotification")
        data class ReadAllNotification(
            override val id: String,
        ) : Main()

        @Serializable
        @SerialName("unreadMessagingMessage")
        data class UnreadMessagingMessage(
            override val id: String,
            val body: StreamingEvent
        ) : Main()

        @Serializable
        @SerialName("mention")
        data class Mention(
            override val id: String,
            val body: NoteDTO
        ) : Main()

        @Serializable
        @SerialName("unreadMention")
        data class UnreadMention(
            override val id: String
        ) : Main()

        @Serializable
        @SerialName("meUpdated")
        data class MeUpdated(
            override val id: String,
            val body: UserDTO

            ) : Main()


    }
}

@Serializable
@SerialName("noteUpdated")
data class NoteUpdated (
    val body: Body
) : StreamingEvent() {


    @Serializable
    sealed class Body{
        abstract val id: String

        @Serializable
        @SerialName("reacted")
        data class Reacted (
            override val id: String,
            val body: Body
        ) : Body() {

            @Serializable
            data class Body(
                val reaction: String,
                val userId: String
            )
        }

        @Serializable
        @SerialName("unreacted")
        data class Unreacted (
            override val id: String,
            val body: Body
        ) : Body() {

            @Serializable
            data class Body(
                val reaction: String,
                val userId: String,
            )
        }

        @Serializable
        @SerialName("pollVoted")
        data class PollVoted(
            override val id: String,
            val body: Body
        ) : Body() {

            @Serializable
            data class Body(
                val choice: Int,
                val userId: String
            )

        }

        @Serializable
        @SerialName("deleted")
        data class Deleted(override val id: String, val body: Body) : Body() {

            @Serializable
            data class Body(
                @Serializable(with = DateSerializer::class)
                val deletedAt: Date
            )
        }
    }


}