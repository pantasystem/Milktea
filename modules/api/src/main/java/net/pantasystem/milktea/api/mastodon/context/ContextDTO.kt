package net.pantasystem.milktea.api.mastodon.context

import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO

@kotlinx.serialization.Serializable
data class ContextDTO (
    @SerialName("ancestors")
    val ancestors: List<TootStatusDTO>,

    @SerialName("descendants")
    val descendants: List<TootStatusDTO>
)