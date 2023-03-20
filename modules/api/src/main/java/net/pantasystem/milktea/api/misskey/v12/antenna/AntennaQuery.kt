package net.pantasystem.milktea.api.misskey.v12.antenna

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AntennaQuery(
    @SerialName("i")
    val i: String,

    @SerialName("antennaId")
    val antennaId: String?,

    @SerialName("limit")
    val limit: Int?
)