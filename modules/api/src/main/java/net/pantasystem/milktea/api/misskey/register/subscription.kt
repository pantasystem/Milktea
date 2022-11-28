package net.pantasystem.milktea.api.misskey.register


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serializable as JSerializable

@Serializable
data class Subscription(
    val i: String,
    val endpoint: String,
    val auth: String,
    @SerialName("publickey") val publicKey: String
) : JSerializable



@Serializable
data class UnSubscription(
    val i: String,
    val endpoint: String
) : JSerializable