package jp.panta.misskeyandroidclient.model.auth.custom

import kotlinx.serialization.Serializable

@Serializable
data class ShowApp(
    val i: String,
    val appId: String
)