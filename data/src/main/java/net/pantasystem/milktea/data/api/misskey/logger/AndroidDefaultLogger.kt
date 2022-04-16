package net.pantasystem.milktea.data.api.misskey.logger

import android.util.Log
import jp.panta.misskeyandroidclient.BuildConfig
import jp.panta.misskeyandroidclient.Logger

class AndroidDefaultLogger(
    override val defaultTag: String
) : Logger{

    override fun debug(msg: String, tag: String, e: Throwable?) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg, e)
        }
    }

    override fun error(msg: String, e: Throwable?, tag: String) {
        Log.e(tag, msg, e)
    }

    override fun info(msg: String, tag: String, e: Throwable?) {
        Log.i(tag, msg, e)
    }

    override fun warning(msg: String, tag: String, e: Throwable?) {
        Log.w(tag, msg, e)
    }

    object Factory : Logger.Factory {
        override fun create(tag: String): Logger {
            return AndroidDefaultLogger(tag)
        }
    }

}