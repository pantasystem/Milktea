package jp.panta.misskeyandroidclient.streaming

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
@Serializable
data class Send(val body: Body)

// FIXME Kotlin serializationの仕様による出力とJSONの入力が合わないことが発覚した
@Serializable
sealed class Body {


    @SerialName("connect")
    @Serializable
    data class Connect(
        val body: Body,
        // type(channel)
    ) : Body() {

        @Serializable
        enum class Type {
            @SerialName("main") MAIN,
            @SerialName("homeTimeline") HOME_TIMELINE,
            @SerialName("localTimeline") LOCAL_TIMELINE,
            @SerialName("hybridTimeline") HYBRID_TIMELINE,
            @SerialName("globalTimeline") GLOBAL_TIMELINE,
        }

        @Serializable
        data class Body (val id: String, val channel: Type)


    }

    @SerialName("disconnect")
    @Serializable
    data class Disconnect(
        val body: Disconnect.Body
    ) : Body(){
        @Serializable
        data class Body(val id: String)
    }

    @SerialName("sn")
    @Serializable
    data class SubscribeNote(
        val body: Body
    ) : Body() {

        @Serializable
        data class Body(
            @SerialName("id") val noteId: String
        )
    }

    @SerialName("un")
    @Serializable
    data class UnSubscribeNote(
        val body: Body
    ) : Body(){
        @Serializable
        data class Body(
            @SerialName("id") val noteId: String
        )
    }
}