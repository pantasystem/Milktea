package jp.panta.misskeyandroidclient.streaming

import com.google.gson.annotations.SerializedName
import java.util.*

sealed class SendBody {
    sealed class Connect : SendBody(){
        abstract val id: String
        class Main(
            override val id: String = UUID.randomUUID().toString()
        ) : Connect()

        class HomeTimeline(
            override val id: String = UUID.randomUUID().toString()
        ) : Connect()

        class GlobalTimeline(
            override val id: String = UUID.randomUUID().toString()
        ) : Connect()

        class HybridTimeline(
            override val id: String = UUID.randomUUID().toString()
        ) : Connect()

        class LocalTimeline(
            override val id: String = UUID.randomUUID().toString()
        ) : Connect()
    }

    data class Disconnect(
        val id: String
    ) : SendBody()


    class SubscribeNote(
        @SerializedName("id") val noteId: String
    ) : SendBody()
    class UnSubscribeNote(
        @SerializedName("id") val noteId: String
    ) : SendBody()


}

