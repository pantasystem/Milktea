package net.pantasystem.milktea.setting.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.lang.Boolean.parseBoolean
import java.lang.Float.parseFloat
import java.lang.Integer.parseInt
import java.lang.Long.parseLong

@Suppress("IMPLICIT_CAST_TO_ANY")
open class MultiTypeSharedPreferenceLiveData(
    val sharedPreferences: SharedPreferences,
    val key: String,
    val default: String,
    val type: Type
): MutableLiveData<String>(){

    enum class Type{
        INTEGER, STRING, BOOLEAN, FLOAT, LONG
    }

    override fun setValue(value: String?) {
        super.setValue(value)
        try{
            sharedPreferences.edit().apply {
                when(type){
                    Type.INTEGER -> {
                        value?.let{
                            putInt(key, parseInt(value))
                        }
                    }
                    Type.BOOLEAN -> {
                        value?.let{
                            putBoolean(key, parseBoolean(it))
                        }
                    }
                    Type.FLOAT -> {
                        value?.let{
                            putFloat(key, parseFloat(it))
                        }
                    }
                    Type.LONG -> {
                        value?.let{
                            putLong(key, parseLong(it))
                        }
                    }
                    Type.STRING ->{
                        putString(key, value)
                    }
                }

            }.apply()

        }catch(e: Exception){
            Log.e("MultiTypePLD", "apply error", e)
        }


    }

    override fun getValue(): String? {
        return when(type){
                Type.INTEGER -> {
                    sharedPreferences.getInt(key, parseInt(default))

                }
                Type.BOOLEAN -> {

                    sharedPreferences.getBoolean(key, parseBoolean(default))
                }
                Type.FLOAT -> {
                    sharedPreferences.getFloat(key, parseFloat(default))
                }
                Type.LONG -> {
                    sharedPreferences.getLong(key, parseLong(default))
                }
                Type.STRING ->{
                    sharedPreferences.getString(key, default)
                }
            }?.toString()
    }

}