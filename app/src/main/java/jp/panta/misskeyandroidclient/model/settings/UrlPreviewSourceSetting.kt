package jp.panta.misskeyandroidclient.model.settings

import android.content.SharedPreferences
import java.util.regex.Pattern

class UrlPreviewSourceSetting(
    val sharedPreferences: SharedPreferences
) {

    companion object{
        const val URL_PREVIEW_SOURCE_TYPE_KEY = "jp.panta.misskeyandroidclient.model.settings.URL_PREVIEW_SOURCE_TYPE"
        const val MISSKEY = 0
        const val SUMMALY = 1
        const val APP = 2
        const val SUMMALY_SERVER_URL_KEY = "jp.panta.misskeyandroidclient.model.settings.SUMMALY_SERVER_URL_KEY"

    }

    private val urlPattern = Pattern.compile("""(https)(://)([-_.!~*'()\[\]a-zA-Z0-9;/?:@&=+${'$'},%#]+)""")


    fun getSummalyUrl(): String?{
        return sharedPreferences.getString(SUMMALY_SERVER_URL_KEY, null)?.let{ url ->
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
            edit.putString(SUMMALY_SERVER_URL_KEY, url)
            edit.putInt(URL_PREVIEW_SOURCE_TYPE_KEY, SUMMALY)
            edit.apply()
            true
        }else{
            false
        }
    }

    fun getSourceType(): Int{
        val type = sharedPreferences.getInt(URL_PREVIEW_SOURCE_TYPE_KEY, MISSKEY)
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