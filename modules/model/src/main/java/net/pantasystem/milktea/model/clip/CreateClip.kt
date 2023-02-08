package net.pantasystem.milktea.model.clip

data class CreateClip(
    val name: String,
    val description: String?,
    val isPublic: Boolean,
    val accountId: Long
)

typealias UpdateClip = CreateClip