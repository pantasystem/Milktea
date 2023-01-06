package net.pantasystem.milktea.common_android.resource

import android.content.Context
import androidx.annotation.StringRes

sealed class StringSource {
    abstract fun getString(context: Context): String

    private data class Raw(private val text: String) : StringSource() {
        override fun getString(context: Context): String = text
    }

    private data class Resource(@StringRes private val textRes: Int) : StringSource() {
        override fun getString(context: Context): String = context.getString(textRes)
    }

    private class FormatResource(@StringRes private val textRes: Int, private vararg val formatArgs: Any) : StringSource() {
        override fun getString(context: Context): String {
            val formatArgs = formatArgs.map { if (it is StringSource) it.getString(context) else it }.toTypedArray()
            return context.getString(textRes, *formatArgs)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FormatResource

            if (textRes != other.textRes) return false
            if (!formatArgs.contentEquals(other.formatArgs)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = textRes
            result = 31 * result + formatArgs.contentHashCode()
            return result
        }
    }

    private data class StringSourceList(private val list: List<StringSource>) : StringSource() {
        override fun getString(context: Context): String = list.joinToString(separator = "") { it.getString(context) }
    }

    operator fun plus(other: StringSource): StringSource = StringSourceList(listOf(this, other))

    companion object {
        operator fun invoke(text: String): StringSource = Raw(text)

        operator fun invoke(@StringRes textRes: Int): StringSource = Resource(textRes)

        operator fun invoke(@StringRes textRes: Int, vararg formatArgs: Any): StringSource = FormatResource(textRes, *formatArgs)
    }
}