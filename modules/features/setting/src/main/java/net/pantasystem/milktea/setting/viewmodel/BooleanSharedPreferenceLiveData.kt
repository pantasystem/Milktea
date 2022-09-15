package net.pantasystem.milktea.setting.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData

class BooleanSharedPreferenceLiveData(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val default: Boolean
) : MutableLiveData<Boolean>(){


    override fun setValue(value: Boolean) {
        super.setValue(value)

        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()

    }

    override fun getValue(): Boolean {
        //return super.getValue()
        return sharedPreferences.getBoolean(key, default)
    }
}