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
    @SerialName("body")
    val body: ChannelBody
) : StreamingEvent()

@Serializable
sealed class ChannelBody : StreamingEvent(){

    abstract val id: String

    @Serializable
    @SerialName("note")
    data class ReceiveNote(
        @SerialName("id")
        override val id: String,

        @SerialName("body")
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
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            val body: NotificationDTO
        ) : Main()

        @Serializable
        @SerialName("readAllNotifications")
        data class ReadAllNotifications(
            @SerialName("id")
            override val id: String,
        ) : Main()

        @Serializable
        @SerialName("unreadNotification")
        data class UnreadNotification(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            val body: NotificationDTO
        ) : Main()

        @Serializable
        @SerialName("unreadMessagingMessage")
        data class UnreadMessagingMessage(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            override val body: MessageDTO
        ) : Main(), HavingMessagingBody

        @Serializable
        @SerialName("readAllMessagingMessage")
        data class ReadAllMessagingMessages(@SerialName("id") override val id: String) : Main()

        @Serializable
        @SerialName("readAllUnreadSpecifiedNotes")
        data class ReadAllUnreadSpecifiedNotes(@SerialName("id") override val id: String) : Main()

        @Serializable
        @SerialName("readAllChannels")
        data class ReadAllChannels(@SerialName("id") override val id: String) : Main()

        @Serializable
        @SerialName("readAllUnreadMentions")
        data class ReadAllUnreadMentions(@SerialName("id") override val id: String) : Main()

        @Serializable
        @SerialName("readNotifications")
        data class ReadNotifications(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            val body: List<String>
        ) : Main()

        @Serializable
        @SerialName("unreadAntenna")
        data class UnreadAntenna(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            val body: AntennaDTO
        ) : Main()

        @Serializable
        @SerialName("mention")
        data class Mention(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            override val body: NoteDTO
        ) : Main(), HavingNoteBody


        @Serializable
        @SerialName("unreadMention")
        data class UnreadMention(
            @SerialName("id")
            override val id: String,
            @SerialName("body") val noteId: String
        ) : Main()

        @Serializable
        @SerialName("renote")
        data class Renote(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            override val body: NoteDTO
        ) : Main(), HavingNoteBody

        @Serializable
        @SerialName("messagingMessage")
        data class MessagingMessage(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            override val body: MessageDTO
        ) : Main(), HavingMessagingBody

        @Serializable
        @SerialName("meUpdated")
        data class MeUpdated(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            override val body: UserDTO,
        ) : Main(),
            HavingUserBody

        @Serializable
        @SerialName("unfollow")
        data class UnFollow(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            override val body: UserDTO,
        ) : Main(), HavingUserBody


        @Serializable
        @SerialName("followed")
        data class Follow(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            override val body: UserDTO
        ) : Main(), HavingUserBody

        @Serializable
        @SerialName("follow")
        data class Followed(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            override val body: UserDTO
        ) : Main(), HavingUserBody

        @Serializable
        @SerialName("fileUpdated")
        data class FileUpdated(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            val file: FilePropertyDTO
        ) : Main()

        @Serializable
        @SerialName("driveFileCreated")
        data  class DriveFileCreated(
            @SerialName("id")
            override val id: String
        ) : Main()

        @Serializable
        @SerialName("fileDeleted")
        data class FileDeleted(
            @SerialName("id")
            override val id: String
        ) : Main()

        @Serializable
        @SerialName("readAntenna")
        data class ReadAntenna(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            val body: AntennaDTO
        ) : Main()

        @Serializable
        @SerialName("readAllAntennas")
        data class ReadAllAntennas(
            @SerialName("id")
            override val id: String
        ) : Main()

        @Serializable
        @SerialName("reply")
        data class Reply(
            @SerialName("id")
            override val id: String,
            @SerialName("body")
            val body: NoteDTO
        ) : Main()
    }
}

@Serializable
@SerialName("noteUpdated")
data class NoteUpdated(
    @SerialName("body")
    val body: Body
) : StreamingEvent() {


    @Serializable
    sealed class Body{
        abstract val id: String

        @Serializable
        @SerialName("reacted")
        data class Reacted (
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            val body: Body
        ) : Body() {

            @Serializable
            data class Body(
                @SerialName("reaction")
                val reaction: String,

                @SerialName("userId")
                val userId: String,

                @SerialName("emoji")
                val emoji: Emoji? = null
            )
        }

        @Serializable
        @SerialName("unreacted")
        data class Unreacted (
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            val body: Body
        ) : Body() {

            @Serializable
            data class Body(
                @SerialName("reaction")
                val reaction: String,

                @SerialName("userId")
                val userId: String,
            )
        }

        @Serializable
        @SerialName("pollVoted")
        data class PollVoted(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            val body: Body
        ) : Body() {

            @Serializable
            data class Body(
                @SerialName("choice")
                val choice: Int,

                @SerialName("userId")
                val userId: String
            )

        }

        @Serializable
        @SerialName("deleted")
        data class Deleted(
            @SerialName("id")
            override val id: String,

            @SerialName("body")
            val body: Body,
        ) : Body() {

            @Serializable
            data class Body @OptIn(ExperimentalSerializationApi::class) constructor(
                @Serializable(with = DateSerializer::class)
                @SerialName("deletedAt")
                val deletedAt: Date
            )
        }
    }


}

@SerialName("emojiAdded")
@Serializable
data class EmojiAdded(
    @SerialName("body")
    val body: Body,
) : StreamingEvent() {
    @Serializable
    data class Body(
        @SerialName("emoji")
        val emoji: Emoji
    )
}

@SerialName("emojiDeleted")
@Serializable
data class EmojiDeleted(
    @SerialName("body")
    val body: Body
) : StreamingEvent() {
    @Serializable
    data class Body(
        @SerialName("emojis")
        val emojis: List<Emoji>,
    )
}

@SerialName("emojiUpdated")
@Serializable
data class EmojiUpdated(
    @SerialName("body")
    val body: Body
) : StreamingEvent() {

    @Serializable
    data class Body(
        @SerialName("emojis")
        val emojis: List<Emoji>,
    )
}