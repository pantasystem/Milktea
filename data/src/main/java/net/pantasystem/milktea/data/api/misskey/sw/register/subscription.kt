package net.pantasystem.milktea.data.api.misskey.sw.register

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

data class UnSubscription(
    val i: String,
    val endpoint: String
) : JSerializable