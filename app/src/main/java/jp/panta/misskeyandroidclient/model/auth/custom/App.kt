package jp.panta.misskeyandroidclient.model.auth.custom

import java.io.Serializable

data class App(
    val id: String,
    val name: String,
    val callbackUrl: String,
    val isAuthorized: Boolean,
    val permission: List<String>,
    val secret: String?
): Serializable