package jp.panta.misskeyandroidclient.impl

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import net.pantasystem.milktea.common.BuildConfig
import net.pantasystem.milktea.common.Logger

class AndroidDefaultLogger(
    override val defaultTag: String
) : Logger {

    override fun debug(tag: String, e: Throwable?, message: () -> String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message(), e)
        }
    }

    override fun debug(msg: String, tag: String, e: Throwable?) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg, e)
        }
    }

    override fun error(msg: String, e: Throwable?, tag: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg, e)
        } else {
            if (e == null) {
                FirebaseCrashlytics.getInstance().log("$tag: E:$msg")
            } else {
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
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