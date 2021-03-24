package jp.panta.misskeyandroidclient.streaming

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
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
        }

        @Serializable
        data class Body (val id: String, val channel: Type)


    }

    @SerialName("readNotification")
    @Serializable
    data class ReadNotification(
        val body: Body
    ) : Send() {

        /**
         * @param id 通知のId
         */
        @Serializable
        data class Body(val id: String)
    }



    @SerialName("disconnect")
    @Serializable
    data class Disconnect(
        val body: Body
    ) : Send(){
        @Serializable
        data class Body(val id: String)
    }

    @SerialName("subNote")
    @Serializable
    data class SubscribeNote(
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

fun String.fromJson(): Send {
    return Json.decodeFromString(this)
}