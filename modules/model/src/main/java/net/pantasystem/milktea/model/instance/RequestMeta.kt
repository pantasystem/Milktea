package net.pantasystem.milktea.model.instance

import kotlinx.serialization.Serializable

@Serializable
data class RequestMeta(val detail: Boolean = true)