package net.pantasystem.milktea.model.instance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestMeta(@SerialName("detail") val detail: Boolean = true)