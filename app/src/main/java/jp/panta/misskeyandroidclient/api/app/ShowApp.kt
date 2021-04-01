package jp.panta.misskeyandroidclient.api.app

import kotlinx.serialization.Serializable

@Serializable
data class ShowApp(
    val i: String,
    val appId: String
)