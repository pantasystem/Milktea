package net.pantasystem.milktea.api_streaming


import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.api.misskey.messaging.MessageDTO
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.api.misskey.v12.antenna.AntennaDTO
import net.pantasystem.milktea.common.serializations.DateSerializer
import net.pantasystem.milktea.model.emoji.Emoji
import java.util.*




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

        interface HavingMessagingBody {
            val body: MessageDTO
        }

        interface HavingNoteBody {
            val body: NoteDTO
        }

        interface HavingUserBody {
            val body: UserDTO
        }

        @Serializable
        @SerialName("notification")
        data class Notification(
            override val id: String,
            val body: NotificationDTO
        ) : Main()

        @Serializable
        @SerialName("readAllNotifications")
        data class ReadAllNotifications(
            override val id: String,
        ) : Main()

        @Serializable
        @SerialName("unreadNotification")
        data class UnreadNotification(
            override val id: String,
            val body: NotificationDTO
        ) : Main()

        @Serializable
        @SerialName("unreadMessagingMessage")
        data class UnreadMessagingMessage(
            override val id: String,
            override val body: MessageDTO
        ) : Main(), HavingMessagingBody

        @Serializable
        @SerialName("readAllMessagingMessage")
        data class ReadAllMessagingMessages(override val id: String) : Main()

        @Serializable
        @SerialName("readAllUnreadSpecifiedNotes")
        data class ReadAllUnreadSpecifiedNotes(override val id: String) : Main()

        @Serializable
        @SerialName("readAllChannels")
        data class ReadAllChannels(override val id: String) : Main()

        @Serializable
        @SerialName("readAllUnreadMentions")
        data class ReadAllUnreadMentions(override val id: String) : Main()

        @Serializable
        @SerialName("readNotifications")
        data class ReadNotifications(
            override val id: String,
            val body: List<String>
        ) : Main()

        @Serializable
        @SerialName("unreadAntenna")
        data class UnreadAntenna(
            override val id: String,
            val body: AntennaDTO
        ) : Main()

        @Serializable
        @SerialName("mention")
        data class Mention(
            override val id: String,
            override val body: NoteDTO
        ) : Main(), HavingNoteBody


        @Serializable
        @SerialName("unreadMention")
        data class UnreadMention(
            override val id: String,
            @SerialName("body") val noteId: String
        ) : Main()

        @Serializable
        @SerialName("renote")
        data class Renote(
            override val id: String,
            override val body: NoteDTO
        ) : Main(), HavingNoteBody

        @Serializable
        @SerialName("messagingMessage")
        data class MessagingMessage(
            override val id: String,
            override val body: MessageDTO
        ) : Main(), HavingMessagingBody

        @Serializable
        @SerialName("meUpdated")
        data class MeUpdated(override val id: String, override val body: UserDTO) : Main(),
            HavingUserBody

        @Serializable
        @SerialName("unfollow")
        data class UnFollow(
            override val id: String,
            override val body: UserDTO
        ) : Main(), HavingUserBody


        @Serializable
        @SerialName("followed")
        data class Follow(
            override val id: String,
            override val body: UserDTO
        ) : Main(), HavingUserBody

        @Serializable
        @SerialName("follow")
        data class Followed(
            override val id: String,
            override val body: UserDTO
        ) : Main(), HavingUserBody

        @Serializable
        @SerialName("fileUpdated")
        data class FileUpdated(
            override val id: String,
            val file: FilePropertyDTO
        ) : Main()

        @Serializable
        @SerialName("driveFileCreated")
        data  class DriveFileCreated(
            override val id: String
        ) : Main()

        @Serializable
        @SerialName("fileDeleted")
        data class FileDeleted(
            override val id: String
        ) : Main()

        @Serializable
        @SerialName("readAntenna")
        data class ReadAntenna(
            override val id: String,
            val body: AntennaDTO
        ) : Main()

        @Serializable
        @SerialName("readAllAntennas")
        data class ReadAllAntennas(
            override val id: String
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
                val userId: String,
                val emoji: Emoji? = null
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
            data class Body @OptIn(ExperimentalSerializationApi::class) constructor(
                @Serializable(with = DateSerializer::class)
                val deletedAt: Date
            )
        }
    }


}

@SerialName("emojiAdded")
@Serializable
data class EmojiAdded(
    val body: Body,
) : StreamingEvent() {
    @Serializable
    data class Body(
        val emoji: Emoji
    )
}