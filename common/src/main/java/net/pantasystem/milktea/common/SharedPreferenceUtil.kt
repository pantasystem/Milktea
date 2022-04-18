package net.pantasystem.milktea.common

import android.content.Context
import android.content.SharedPreferences

fun Context.getPreferenceName(): String{
    return this.packageName + "_preferences"
}
fun Context.getPreferences(): SharedPreferences {
    return this.getSharedPreferences(this.getPreferenceName(), Context.MODE_PRIVATE)
}