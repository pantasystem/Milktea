package net.pantasystem.milktea.model.messaging

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestMessageHistory(
    @SerialName("i") val i: String,
    @SerialName("limit") val limit: Int? = null,
    @SerialName("group") val group: Boolean? = null
)