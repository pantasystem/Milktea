package net.pantasystem.milktea.model.setting

sealed interface Theme {
    object White : Theme
    object Black : Theme
    object Dark : Theme
    object Bread : Theme
    companion object
}


fun Theme.isNightTheme(): Boolean {
    return this is Theme.Black || this is Theme.Dark
}