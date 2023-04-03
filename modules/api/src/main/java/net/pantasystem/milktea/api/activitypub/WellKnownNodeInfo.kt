package net.pantasystem.milktea.api.activitypub

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class WellKnownNodeInfo(
    @SerialName("links")
    val links: List<Link>
) {
    @kotlinx.serialization.Serializable
    data class Link(
        @SerialName("rel")
        val rel: String,

        @SerialName("href")
        val href: String,
    )
}