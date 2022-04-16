package net.pantasystem.milktea.data.model.channel

data class CreateChannel(
    val name: String,
    val description: String?,
    val accountId: Long,
    val bannerId: String?
)