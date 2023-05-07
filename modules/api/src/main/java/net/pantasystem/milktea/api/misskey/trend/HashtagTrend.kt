package net.pantasystem.milktea.api.misskey.trend

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class HashtagTrend(
    @SerialName("tag") val tag: String,
    @SerialName("chart") val chart: List<Int>,
    @SerialName("usesCount") val usesCount: Int,
)