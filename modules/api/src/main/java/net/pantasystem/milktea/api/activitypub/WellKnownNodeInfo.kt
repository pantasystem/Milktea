package net.pantasystem.milktea.api.activitypub

@kotlinx.serialization.Serializable
data class WellKnownNodeInfo(
    val links: List<Link>
) {
    @kotlinx.serialization.Serializable
    data class Link(
        val rel: String,
        val href: String,
    )
}