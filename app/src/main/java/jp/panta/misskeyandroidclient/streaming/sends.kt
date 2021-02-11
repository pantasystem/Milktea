package jp.panta.misskeyandroidclient.streaming

import com.google.gson.annotations.SerializedName
import java.util.*


sealed class Connect{
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
)


class SubscribeNote(
    @SerializedName("id") val noteId: String
)
class UnSubscribeNote(
    @SerializedName("id") val noteId: String
)


