package jp.panta.misskeyandroidclient.api.sw.register

import java.io.Serializable as JSerializable

data class Subscription(
    val endpoint: String,
    val auth: String,
    val publicKey: String
) : JSerializable

data class SubscriptionState(
    val state: String,
    val key: String
)