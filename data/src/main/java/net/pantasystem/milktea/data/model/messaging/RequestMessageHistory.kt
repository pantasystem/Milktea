package net.pantasystem.milktea.data.model.messaging

import kotlinx.serialization.Serializable

@Serializable
data class RequestMessageHistory(
    val i: String,
    val limit: Int? = null,
    val group: Boolean? = null
)