package net.pantasystem.milktea.common

import java.security.MessageDigest

object Hash {
    private const val HEX_CHARS = "0123456789ABCDEF"

    fun sha256(input: String) = hashString("SHA-256", input)

    private fun hashString(type: String, input: String): String {
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }
        return result.toString()
    }
}