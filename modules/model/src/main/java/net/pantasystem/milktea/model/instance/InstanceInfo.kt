package net.pantasystem.milktea.model.instance

data class InstanceInfo(
    val id: String,
    val host: String,
    val name: String?,
    val description: String?,
    val clientMaxBodyByteSize: Long?,
    val iconUrl: String?,
    val themeColor: String?,
)

