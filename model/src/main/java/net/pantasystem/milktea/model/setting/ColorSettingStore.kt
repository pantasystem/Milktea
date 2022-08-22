package net.pantasystem.milktea.model.setting

import android.content.SharedPreferences

class ColorSettingStore(private val sharedPreferences: SharedPreferences) {
    companion object{
        const val SURFACE_COLOR_OPAQUE_KEY = "jp.panta.misskeyandroidclient.model.settings.SURFACE_COLOR_OPAQUE_KEY"
    }

    private var mSurfaceColorOpaque: Int? = null

    var surfaceColorOpaque: Int
        get() {
            return mSurfaceColorOpaque?: sharedPreferences.getInt(SURFACE_COLOR_OPAQUE_KEY, 0xff)
        }
        set(value) {
            if(value in 0..255){
                val e = sharedPreferences.edit()
                e.putInt(SURFACE_COLOR_OPAQUE_KEY, value)
                e.apply()
            }

        }


    init{
        sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            when(key){
                SURFACE_COLOR_OPAQUE_KEY ->{
                    mSurfaceColorOpaque = sharedPreferences.getInt(SURFACE_COLOR_OPAQUE_KEY, 0xff)
                }
            }
        }
    }

}