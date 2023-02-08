package net.pantasystem.milktea.common_navigation

interface ClipListNavigation : ActivityNavigation<ClipListNavigationArgs>

data class ClipListNavigationArgs(
    val accountId: Long? = null,
    val mode: Mode = Mode.View
) {
    enum class Mode {
        AddToTab,
        View,
    }
}