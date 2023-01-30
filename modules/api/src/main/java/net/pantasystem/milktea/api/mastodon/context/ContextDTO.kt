package net.pantasystem.milktea.api.mastodon.context

import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO

@kotlinx.serialization.Serializable
data class ContextDTO (
    val ancestors: List<TootStatusDTO>,
    val descendants: List<TootStatusDTO>
)