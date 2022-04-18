package net.pantasystem.milktea.api.mastodon.instance

import kotlinx.serialization.Serializable

@Serializable
data class Instance (
    val uri: String,
    val title: String,
    val description: String,
    val email: String,
    val version: String,
    val urls: Map<String, String>,
    val languages: List<String>
)
