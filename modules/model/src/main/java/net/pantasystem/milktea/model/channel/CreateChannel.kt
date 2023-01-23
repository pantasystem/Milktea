package net.pantasystem.milktea.model.channel

data class CreateChannel(
    val name: String,
    val description: String?,
    val accountId: Long,
    val bannerId: String?
)