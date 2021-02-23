package jp.panta.misskeyandroidclient.api.logger

import android.util.Log
import jp.panta.misskeyandroidclient.Logger

class AndroidDefaultLogger(
    override val defaultTag: String
) : Logger{

    override fun debug(msg: String, tag: String, e: Exception?) {
        Log.d(tag, msg, e)
    }

    override fun error(msg: String, e: Exception?, tag: String) {
        Log.e(tag, msg, e)
    }

    override fun info(msg: String, tag: String, e: Exception?) {
        Log.i(tag, msg, e)
    }

    override fun warning(msg: String, tag: String, e: Exception?) {
        Log.w(tag, msg, e)
    }


}