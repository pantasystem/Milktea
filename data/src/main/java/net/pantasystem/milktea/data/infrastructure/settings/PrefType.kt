package net.pantasystem.milktea.data.infrastructure.settings

sealed interface PrefType {
    data class StrPref(
        val value: String?
    ) : PrefType

    data class BoolPref(
        val value: Boolean
    ) : PrefType

    data class IntPref(
        val value: Int
    ) : PrefType

}
