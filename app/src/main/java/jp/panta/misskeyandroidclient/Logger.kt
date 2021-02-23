package jp.panta.misskeyandroidclient

import jp.panta.misskeyandroidclient.api.logger.AndroidDefaultLogger

/**
 * Android標準のLogはAndroidに依存してしまっているため、
 * ユニットテストで邪魔になることが多いのと環境によって、出力先を変えたいので抽象化することにした。
 */
interface Logger {
    val defaultTag: String
    fun debug(msg: String, tag: String = defaultTag, e: Exception? = null)

    fun error(msg: String, e: Throwable? = null, tag: String = defaultTag)

    fun info(msg: String, tag: String = defaultTag, e: Exception? = null)

    fun warning(msg: String, tag: String = defaultTag, e: Exception? = null)

    interface Factory {
        fun create(tag: String = "MisskeyApp"): Logger
    }
}