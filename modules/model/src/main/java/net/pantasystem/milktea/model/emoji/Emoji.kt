package net.pantasystem.milktea.model.emoji

import java.io.Serializable

@kotlinx.serialization.Serializable
data class Emoji(
    val id: String? = null,
    val name: String,
    val host: String? = null,
    val url: String? = null,
    val uri: String? = null,
    val type: String? = null,
    val category: String? = null,
    val aliases: List<String>? = null

): Serializable{

    constructor(name: String) : this(null, name, null, null, null, null, null)
}