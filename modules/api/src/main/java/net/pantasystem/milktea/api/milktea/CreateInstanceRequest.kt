package net.pantasystem.milktea.api.milktea

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CreateInstanceRequest(@SerialName("host") val host: String)