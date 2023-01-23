package net.pantasystem.milktea.model.setting

@kotlinx.serialization.Serializable
data class WebClientBaseCache(
    val reactions: List<String>
)