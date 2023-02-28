package net.pantasystem.milktea.common

/**
 * Android標準のLogはAndroidに依存してしまっているため、
 * ユニットテストで邪魔になることが多いのと環境によって、出力先を変えたいので抽象化することにした。
 */
interface Logger {
    val defaultTag: String
    fun debug(msg: String, tag: String = defaultTag, e: Throwable? = null)

    fun debug(tag: String = defaultTag, e: Throwable? = null, message: () -> String)

    fun error(msg: String, e: Throwable? = null, tag: String = defaultTag)

    fun info(msg: String, tag: String = defaultTag, e: Throwable? = null)

    fun warning(msg: String, tag: String = defaultTag, e: Throwable? = null)

    interface Factory {
        fun create(tag: String = "MisskeyApp"): Logger
    }
}