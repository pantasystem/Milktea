package net.pantasystem.milktea.model.setting

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

    data class FloatPref(
        val value: Float
    ) : PrefType

}
