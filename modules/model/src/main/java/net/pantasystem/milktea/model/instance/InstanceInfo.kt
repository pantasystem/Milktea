package net.pantasystem.milktea.model.instance

data class InstanceInfo(
    val id: String,
    val host: String,
    val name: String? = null,
    val description: String? = null,
    val clientMaxBodyByteSize: Long? = null,
    val iconUrl: String? = null,
    val themeColor: String? = null,
)

