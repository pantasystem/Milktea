package net.pantasystem.milktea.api.misskey

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class I(@SerialName("i") val i: String?)