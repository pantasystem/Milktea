package net.pantasystem.milktea.data.infrastructure.settings

import net.pantasystem.milktea.model.setting.UrlPreviewConfig
import net.pantasystem.milktea.model.setting.urlPattern

fun UrlPreviewConfig.Type.toInt(): Int {
    return when (this) {
        is UrlPreviewConfig.Type.Misskey -> {
            UrlPreviewSourceSetting.MISSKEY
        }
        is UrlPreviewConfig.Type.SummalyServer -> {
            UrlPreviewSourceSetting.SUMMALY
        }
        is UrlPreviewConfig.Type.InApp -> {
            UrlPreviewSourceSetting.APP
        }
    }
}

fun UrlPreviewConfig.Type.Companion.from(number: Int, url: String? = null): UrlPreviewConfig.Type {
    return when (number) {
        UrlPreviewSourceSetting.APP -> UrlPreviewConfig.Type.InApp
        UrlPreviewSourceSetting.MISSKEY -> UrlPreviewConfig.Type.Misskey
        UrlPreviewSourceSetting.SUMMALY -> if (url == null || urlPattern.matches(url)) {
            UrlPreviewConfig.Type.Misskey
        } else {
            UrlPreviewConfig.Type.SummalyServer(
                url
            )
        }
        else -> UrlPreviewConfig.Type.Misskey
    }
}