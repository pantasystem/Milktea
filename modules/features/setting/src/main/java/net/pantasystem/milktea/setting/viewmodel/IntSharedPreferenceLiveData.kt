package net.pantasystem.milktea.setting.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData

class IntSharedPreferenceLiveData(
    val sharedPreferences: SharedPreferences,
    val key: String,
    val default: Int
): MutableLiveData<Int>(){

    override fun setValue(value: Int?) {
        super.setValue(value)
        if(value != null){
            sharedPreferences.edit().apply{
                putInt(key, value)
            }.apply()
        }

    }


    override fun getValue(): Int {
        return sharedPreferences.getInt(key, default)
    }
}