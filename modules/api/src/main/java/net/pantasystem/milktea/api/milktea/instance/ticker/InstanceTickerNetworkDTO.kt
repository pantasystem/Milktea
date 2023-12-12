package net.pantasystem.milktea.api.milktea.instance.ticker

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.instance.ticker.InstanceTicker

@Serializable
data class InstanceTickerNetworkDTO(
    @SerialName("uri") val uri: String,
    @SerialName("faviconUrl") val faviconUrl: String?,
    @SerialName("iconUrl") val iconUrl: String?,
    @SerialName("name") val name: String?,
    @SerialName("softwareName") val softwareName: String?,
    @SerialName("softwareVersion") val softwareVersion: String?,
    @SerialName("themeColor") val themeColor: String?,
) {
    fun toModel(): InstanceTicker {
        return InstanceTicker(
            uri = uri,
            faviconUrl = faviconUrl,
            iconUrl = iconUrl,
            name = name,
            softwareName = softwareName,
            softwareVersion = softwareVersion,
            themeColor = themeColor,
        )
    }
}