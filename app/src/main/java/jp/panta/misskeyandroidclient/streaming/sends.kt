package jp.panta.misskeyandroidclient.streaming

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*
/*
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
        data class Main(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "main"
        ) : Connect()

        @Serializable
        data class HomeTimeline(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "homeTimeline"
        ) : Connect()

        @Serializable
        data class GlobalTimeline(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "globalTimeline"
        ) : Connect()

        @Serializable
        data class HybridTimeline(
            override val id: String = UUID.randomUUID().toString(),
            override val channel: String = "hybridTimeline"
        ) : Connect()

        @Serializable
        data class LocalTimeline(
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
*/
// FIXME Kotlin serializationの仕様による出力とJSONの入力が合わないことが発覚した
sealed class Send {

    @SerialName("connect")
    data class Connect(
        val body: Body,
        // type(channel)
    ) : Send() {

        sealed class Body


    }

    @SerialName("sn")
    data class SubscribeNote(
        val body: Body
    ) : Send() {
        data class Body(
            @SerialName("id") val noteId: String
        )
    }

    @SerialName("un")
    data class UnSubscribeNote(
        val body: Body
    ) {
        data class Body(
            @SerialName("id") val noteId: String
        )
    }
}