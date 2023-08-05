package net.pantasystem.milktea.api.misskey.register


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serializable as JSerializable

@Serializable
data class SubscriptionStateNetworkDTO(
    @SerialName("state") val state: String,
    @SerialName("key") val key: String? = null,
)

@Serializable
data class Subscription(
    @SerialName("i")
    val i: String,

    @SerialName("endpoint")
    val endpoint: String,

    @SerialName("auth")
    val auth: String,

    @SerialName("publickey")
    val publicKey: String
) : JSerializable



@Serializable
data class UnSubscription(
    @SerialName("i")
    val i: String,

    @SerialName("endpoint")
    val endpoint: String
) : JSerializable