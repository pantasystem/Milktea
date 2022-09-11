package net.pantasystem.milktea.common_navigation

interface TimeMachineNavigation : ActivityNavigation<TimeMachineArgs>

data class TimeMachineArgs(
    val initialStartAt: Long
)