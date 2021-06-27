package jp.panta.misskeyandroidclient.api.sw.register

import com.google.gson.annotations.SerializedName
import java.io.Serializable as JSerializable

data class Subscription(
    val i: String,
    val endpoint: String,
    val auth: String,
    @SerializedName("publickey") val publicKey: String
) : JSerializable

data class SubscriptionState(
    val state: String,
    val key: String
)