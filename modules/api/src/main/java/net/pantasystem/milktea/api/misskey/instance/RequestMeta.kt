package net.pantasystem.milktea.api.misskey.instance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestMeta(@SerialName("detail") val detail: Boolean = true)