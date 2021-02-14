package jp.panta.misskeyandroidclient.streaming

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Send(
    val body: SendBody,

)

@Serializable
sealed class SendBody {

    @SerialName("connect")
    sealed class Connect : SendBody(){
        abstract val id: String
        abstract val channel: String

        @Serializable
        class Main(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "main"
        ) : Connect()

        @Serializable
        class HomeTimeline(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "homeTimeline"
        ) : Connect()

        @Serializable
        class GlobalTimeline(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "globalTimeline"
        ) : Connect()

        @Serializable
        class HybridTimeline(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "hybridTimeline"
        ) : Connect()

        @Serializable
        class LocalTimeline(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "localTimeline"
        ) : Connect()
    }

    @Serializable
    data class Disconnect(
        val id: String
    ) : SendBody()

    // type sn
    @Serializable
    @SerialName("sn")
    class SubscribeNote(
        @SerializedName("id") val noteId: String
    ) : SendBody()
    // type un
    @Serializable
    @SerialName("un")
    class UnSubscribeNote(
        @SerializedName("id") val noteId: String
    ) : SendBody()


}

