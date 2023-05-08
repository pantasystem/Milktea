package net.pantasystem.milktea.api.misskey.trend

import kotlinx.serialization.SerialName
import net.pantasystem.milktea.model.hashtag.Hashtag

@kotlinx.serialization.Serializable
data class HashtagTrend(
    @SerialName("tag") val tag: String,
    @SerialName("chart") val chart: List<Int>,
    @SerialName("usesCount") val usesCount: Int,
) {
    fun toModel(): Hashtag {
        return Hashtag(
            tag,
            usesCount,
            chart
        )
    }
}