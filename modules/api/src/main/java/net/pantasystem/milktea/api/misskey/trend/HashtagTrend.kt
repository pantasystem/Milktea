package net.pantasystem.milktea.api.misskey.trend

import kotlinx.serialization.SerialName
import net.pantasystem.milktea.model.hashtag.HashTag

@kotlinx.serialization.Serializable
data class HashtagTrend(
    @SerialName("tag") val tag: String,
    @SerialName("chart") val chart: List<Int>,
    @SerialName("usersCount") val usersCount: Int,
) {
    fun toModel(): HashTag {
        return HashTag(
            tag,
            usersCount,
            chart
        )
    }
}