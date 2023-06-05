package net.pantasystem.milktea.data.infrastructure.settings

import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.Theme

fun Theme.toInt(): Int {
    return when (this) {
        is Theme.White -> 0
        is Theme.Black -> 1
        is Theme.Dark -> 2
        is Theme.Bread -> 3
        Theme.ElephantDark -> 4
    }
}

fun Theme.Companion.from(n: Int): Theme {
    return when (n) {
        0 -> Theme.White
        1 -> Theme.Black
        2 -> Theme.Dark
        3 -> Theme.Bread
        4 -> Theme.ElephantDark
        else -> DefaultConfig.config.theme
    }
}
