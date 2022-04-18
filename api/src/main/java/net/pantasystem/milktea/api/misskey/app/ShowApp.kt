package net.pantasystem.milktea.api.misskey.app

import kotlinx.serialization.Serializable

@Serializable
data class ShowApp(
    val i: String,
    val appId: String
)