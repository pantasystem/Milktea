package jp.panta.misskeyandroidclient.logger

import net.pantasystem.milktea.common.Logger

class TestLogger(
    override val defaultTag: String
) : Logger {

    override fun debug(msg: String, tag: String, e: Throwable?) {
        println("debug:$tag:$msg, error:$e")
    }

    override fun debug(tag: String, e: Throwable?, message: () -> String) {
        println("debug:$tag:${message()}, error:$e")
    }

    override fun error(msg: String, e: Throwable?, tag: String) {
        println("error:$tag:$msg, error:$e")
    }

    override fun info(msg: String, tag: String, e: Throwable?) {
        println("info:$tag:$msg, error:$e")
    }

    override fun warning(msg: String, tag: String, e: Throwable?) {
        println("warning:$tag:$msg, error:$e")

    }

    class Factory : Logger.Factory{
        override fun create(tag: String): Logger {
            return TestLogger(tag)
        }
    }
}