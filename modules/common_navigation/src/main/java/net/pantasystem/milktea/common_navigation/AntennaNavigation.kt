package net.pantasystem.milktea.common_navigation

interface AntennaNavigation : ActivityNavigation<AntennaNavigationArgs> {
}

data class AntennaNavigationArgs(
    val specifiedAccountId: Long? = null,
    val addTabToAccountId: Long? = null,
)