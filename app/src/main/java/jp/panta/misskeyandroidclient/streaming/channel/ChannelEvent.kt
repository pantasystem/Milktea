package jp.panta.misskeyandroidclient.streaming.channel

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.notification.Notification as NotificationDTO

/**
 * bodyとして機能する
 */
sealed class ChannelEvent {


    sealed class Timeline {

    }


    sealed class Main {
        abstract val id: String

        data class Notification(
            override val id: String,
            val body: NotificationDTO
        ) : Main()

        data class ReadAllNotification(
            override val id: String,
        ) : Main()

        data class UnreadMessagingMessage(
            override val id: String,
            val body: Message
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