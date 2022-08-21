package net.pantasystem.milktea.app_store.setting

import android.content.SharedPreferences
import net.pantasystem.milktea.model.setting.Keys
import net.pantasystem.milktea.model.setting.str
import java.util.regex.Pattern

class UrlPreviewSourceSetting(
    val sharedPreferences: SharedPreferences
) {

    companion object{
        const val MISSKEY = 0
        const val SUMMALY = 1
        const val APP = 2

    }

    private val urlPattern = Pattern.compile("""(https)(://)([-_.!~*'()\[\]a-zA-Z0-9;/?:@&=+${'$'},%#]+)""")


    fun getSummalyUrl(): String?{
        return sharedPreferences.getString(Keys.SummalyServerUrl.str(), null)?.let{ url ->
            if(urlPattern.matcher(url).find()){
                url
            }else{
                null
            }
        }
    }

    fun setSummalyUrl(url: String): Boolean{
        val matcher = urlPattern.matcher(url)
        return if(matcher.find()){
            val edit = sharedPreferences.edit()
            edit.putString(Keys.SummalyServerUrl.str(), url)
            edit.putInt(Keys.UrlPreviewSourceType.str(), SUMMALY)
            edit.apply()
            true
        }else{
            false
        }
    }

    fun setSourceType(type: Int){
        val edit = sharedPreferences.edit()
        val srcType = if( type in 0 until 3){
            type
        }else{
            MISSKEY
        }
        edit.putInt(Keys.UrlPreviewSourceType.str(), srcType)
        edit.apply()
    }

    fun getSourceType(): Int{
        val type = sharedPreferences.getInt(Keys.UrlPreviewSourceType.str(), MISSKEY)
        if(type in MISSKEY..APP){
            if(type == SUMMALY && getSummalyUrl() == null){
                return MISSKEY
            }
            return type

        }else{
            return MISSKEY
        }
    }

}