package jp.panta.misskeyandroidclient.viewmodel.setting

import android.content.SharedPreferences
import androidx.annotation.StringRes

abstract class SharedItem<T>: Shared{
    abstract val key: String
    abstract val title: Int
    abstract fun get(sharedPreferences: SharedPreferences): T
    abstract fun save(sharedPreferences: SharedPreferences, element: T)
}