package jp.panta.misskeyandroidclient.logger

import jp.panta.misskeyandroidclient.Logger

class TestLogger(
    override val defaultTag: String
) : Logger{

    override fun debug(msg: String, tag: String, e: Exception?) {
        println("$tag:$msg, error:$e")
    }

    override fun error(msg: String, e: Throwable?, tag: String) {
        println("$tag:$msg, error:$e")
    }

    override fun info(msg: String, tag: String, e: Exception?) {
        println("$tag:$msg, error:$e")
    }

    override fun warning(msg: String, tag: String, e: Exception?) {
        println("$tag:$msg, error:$e")

    }

    class Factory : Logger.Factory{
        override fun create(tag: String): Logger {
            return TestLogger(tag)
        }
    }
}