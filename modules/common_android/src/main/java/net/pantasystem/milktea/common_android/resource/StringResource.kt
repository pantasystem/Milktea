package net.pantasystem.milktea.common_android.resource

import android.content.Context
import androidx.annotation.StringRes

sealed interface StringSource {
    fun build(getStringResource: GetStringResource): String

    private data class Raw(private val text: String) : StringSource {

        override fun build(getStringResource: GetStringResource): String = text
    }

    private data class Resource(@StringRes private val textRes: Int) : StringSource {

        override fun build(getStringResource: GetStringResource): String = getStringResource(textRes)
    }

    private class FormatResource(@StringRes private val textRes: Int, private vararg val formatArgs: Any) : StringSource {
        override fun build(getStringResource: GetStringResource): String {
            val formatArgs = formatArgs.map { if (it is StringSource) it.build(getStringResource) else it }.toTypedArray()
            return getStringResource(textRes, *formatArgs)
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

    private data class StringSourceList(private val list: List<StringSource>) : StringSource {
        override fun build(getStringResource: GetStringResource): String {
            return list.joinToString(separator = "") { it.build(getStringResource) }
        }
    }

    operator fun plus(other: StringSource): StringSource = StringSourceList(listOf(this, other))

    companion object {
        operator fun invoke(text: String): StringSource = Raw(text)

        operator fun invoke(@StringRes textRes: Int): StringSource = Resource(textRes)

        operator fun invoke(@StringRes textRes: Int, vararg formatArgs: Any): StringSource = FormatResource(textRes, *formatArgs)
    }
}

interface GetStringResource {
    operator fun invoke(@StringRes textRes: Int): String

    operator fun invoke(@StringRes textRes: Int, vararg formatArgs: Any): String

}

fun StringSource.getString(context: Context): String {
    return build(object : GetStringResource {
        override fun invoke(textRes: Int): String = context.getString(textRes)

        override fun invoke(textRes: Int, vararg formatArgs: Any): String = context.getString(textRes, *formatArgs)
    })
}