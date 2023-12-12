package net.pantasystem.milktea.model.instance.ticker

data class InstanceTicker(
    val uri: String,
    val faviconUrl: String?,
    val iconUrl: String?,
    val name: String?,
    val softwareName: String?,
    val softwareVersion: String?,
    val themeColor: String?,
) {

    fun isValid(): Boolean {
        return uri.isNotEmpty() && name != null && themeColor != null
    }
}