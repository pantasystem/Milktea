package net.pantasystem.milktea.api_streaming

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 注意：decode時やparse時はSendのserializerを使わないとうまくtypeフィールドが追加されない。
 */
@Serializable
sealed class Send {


    @SerialName("connect")
    @Serializable
    data class Connect(
        @SerialName("body")
        val body: Body,
        // type(channel)
    ) : Send() {

        @Serializable
        enum class Type {
            @SerialName("main") MAIN,
            @SerialName("homeTimeline") HOME_TIMELINE,
            @SerialName("localTimeline") LOCAL_TIMELINE,
            @SerialName("hybridTimeline") HYBRID_TIMELINE,
            @SerialName("globalTimeline") GLOBAL_TIMELINE,
            @SerialName("userList") USER_LIST,
            @SerialName("antenna") ANTENNA,
            @SerialName("channel") CHANNEL,
        }

        @Serializable
        data class Body (
            @SerialName("id")
            val id: String,

            @SerialName("type")
            val channel: Type,

            @SerialName("pong")
            val pong: Boolean = false,

            @SerialName("params")
            val params: Params? = null,
        ) {
            @Serializable
            data class Params(
                @SerialName("listId")
                val listId: String? = null,

                @SerialName("antennaId")
                val antennaId: String? = null,

                @SerialName("channelId")
                val channelId: String? = null,
            )
            init {
                require(channel != Type.USER_LIST || params?.listId != null)
                require(channel != Type.ANTENNA || params?.antennaId != null)
                require(channel != Type.CHANNEL || params?.channelId != null)
            }
        }
    }

    @SerialName("readNotification")
    @Serializable
    data class ReadNotification(
        @SerialName("body")
        val body: Body
    ) : Send() {

        /**
         * @param id 通知のId
         */
        @Serializable
        data class Body(
            @SerialName("id")
            val id: String,
        )
    }



    @SerialName("disconnect")
    @Serializable
    data class Disconnect(
        @SerialName("body")
        val body: Body
    ) : Send(){
        @Serializable
        data class Body(
            @SerialName("id")
            val id: String,
        )
    }

    @SerialName("subNote")
    @Serializable
    data class SubscribeNote(
        @SerialName("body")
        val body: Body
    ) : Send() {

        @Serializable
        data class Body(
            @SerialName("id") val noteId: String
        )
    }

    @SerialName("sr")
    @Serializable
    data class SubscribeAndReadNote(
        @SerialName("body")
        val body: Body
    ) : Send() {

        @Serializable
        data class Body(
            @SerialName("id") val noteId: String
        )
    }

    @SerialName("un")
    @Serializable
    data class UnSubscribeNote(
        @SerialName("body")
        val body: Body
    ) : Send(){
        @Serializable
        data class Body(
            @SerialName("id") val noteId: String
        )
    }
}

fun Send.toJson(): String {
    return Json.encodeToString(this)
}

