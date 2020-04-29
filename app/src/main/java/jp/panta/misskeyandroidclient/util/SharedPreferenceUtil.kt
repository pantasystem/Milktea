package jp.panta.misskeyandroidclient.util

import android.content.Context

fun Context.getPreferenceName(): String{
    return this.packageName + "_preferences"
}