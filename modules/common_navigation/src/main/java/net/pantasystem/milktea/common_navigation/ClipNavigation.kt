package net.pantasystem.milktea.common_navigation

import net.pantasystem.milktea.model.clip.ClipId

interface ClipListNavigation : ActivityNavigation<ClipListNavigationArgs>

data class ClipListNavigationArgs(
    val accountId: Long? = null,
    val mode: Mode = Mode.View,
    val addTabToAccountId: Long? = null,
) {
    enum class Mode {
        AddToTab,
        View,
    }
}

interface ClipDetailNavigation : ActivityNavigation<ClipId>