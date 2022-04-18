package net.pantasystem.milktea.model.channel

data class UpdateChannel(
    val id: Channel.Id,
    val name: String,
    val description: String?,
    val bannerId: String?,
)