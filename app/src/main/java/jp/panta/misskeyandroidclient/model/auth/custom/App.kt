package jp.panta.misskeyandroidclient.model.auth.custom

import java.io.Serializable

@kotlinx.serialization.Serializable
data class App(
    val id: String? = null,
    val name: String,
    val callbackUrl: String? = null,
    val isAuthorized: Boolean? = null,
    val permission: List<String> = emptyList(),
    val secret: String? = null
): Serializable