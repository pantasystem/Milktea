package net.pantasystem.milktea.data.infrastructure.instance.ticker.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.instance.ticker.InstanceTicker
import kotlin.time.Duration.Companion.days


@Entity(
    tableName = "instance_tickers",
    primaryKeys = [
        "uri",
    ],
)
data class InstanceTickerRecord(
    @ColumnInfo("uri") val uri: String,
    @ColumnInfo("favicon_url") val faviconUrl: String?,
    @ColumnInfo("icon_url") val iconUrl: String?,
    @ColumnInfo("name") val name: String?,
    @ColumnInfo("software_name") val softwareName: String?,
    @ColumnInfo("software_version") val softwareVersion: String?,
    @ColumnInfo("theme_color") val themeColor: String?,
    @ColumnInfo("created_at") val cachedAt: Instant,
) {

    companion object {
        fun fromModel(instanceTicker: InstanceTicker): InstanceTickerRecord {
            return InstanceTickerRecord(
                uri = instanceTicker.uri,
                faviconUrl = instanceTicker.faviconUrl,
                iconUrl = instanceTicker.iconUrl,
                name = instanceTicker.name,
                softwareName = instanceTicker.softwareName,
                softwareVersion = instanceTicker.softwareVersion,
                themeColor = instanceTicker.themeColor,
                cachedAt = Clock.System.now(),
            )
        }
    }

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

    fun isRecordExpired(): Boolean {
        return cachedAt < Clock.System.now().minus(7.days)
    }
}