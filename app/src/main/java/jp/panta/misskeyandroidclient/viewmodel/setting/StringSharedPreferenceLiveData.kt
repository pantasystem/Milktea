package jp.panta.misskeyandroidclient.viewmodel.setting

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData

class StringSharedPreferenceLiveData(
    val sharedPreferences: SharedPreferences,
    val key: String,
    val default: String
): MutableLiveData<String>(){

    override fun setValue(value: String?) {
        super.setValue(value)

        sharedPreferences.edit().apply {
            putString(key, value)
        }.apply()
    }

    override fun getValue(): String? {
        return sharedPreferences.getString(key, default)
    }
}