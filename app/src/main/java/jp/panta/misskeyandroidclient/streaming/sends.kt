package jp.panta.misskeyandroidclient.streaming

import com.google.gson.annotations.SerializedName
import java.util.*


data class Send(
    val body: SendBody,
    val type: String = when(body){
        is SendBody.Connect -> "connect"
        is SendBody.Disconnect -> "disconnect"
        is SendBody.SubscribeNote -> "sn"
        is SendBody.UnSubscribeNote -> "un"
    }
)
sealed class SendBody {
    sealed class Connect : SendBody(){
        abstract val id: String
        abstract val channel: String
        class Main(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "main"
        ) : Connect()

        class HomeTimeline(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "homeTimeline"
        ) : Connect()

        class GlobalTimeline(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "globalTimeline"
        ) : Connect()

        class HybridTimeline(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "hybridTimeline"
        ) : Connect()

        class LocalTimeline(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "localTimeline"
        ) : Connect()
    }

    data class Disconnect(
        val id: String
    ) : SendBody()

    // type sn
    class SubscribeNote(
        @SerializedName("id") val noteId: String
    ) : SendBody()
    // type un
    class UnSubscribeNote(
        @SerializedName("id") val noteId: String
    ) : SendBody()


}

