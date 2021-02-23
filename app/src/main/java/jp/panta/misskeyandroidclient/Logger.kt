package jp.panta.misskeyandroidclient

/**
 * Android標準のLogはAndroidに依存してしまっているため、
 * ユニットテストで邪魔になることが多いのと環境によって、出力先を変えたいので抽象化することにした。
 */
interface Logger {
    val defaultTag: String
    fun debug(msg: String, tag: String = defaultTag, e: Exception? = null)

    fun error(msg: String, e: Exception? = null, tag: String = defaultTag)

    fun info(msg: String, tag: String = defaultTag, e: Exception? = null)

    fun warning(msg: String, tag: String = defaultTag, e: Exception? = null)
}