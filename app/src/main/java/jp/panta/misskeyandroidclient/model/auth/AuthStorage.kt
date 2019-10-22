package jp.panta.misskeyandroidclient.model.auth

import android.content.SharedPreferences
import java.util.*

private const val AUTH_STORAGE_SESSION_URL = "jp.panta.misskeyandroidclient.model.auth.AuthStorage.sessionUrl"
private const val AUTH_STORAGE_SESSION_TOKEN = "jp.panta.misskeyandroidclient.model.auth.AuthStorage.sessionToken"
private const val AUTH_STORAGE_SESSION_TIME_STAMP = "jp.panta.misskeyandroidclient.model.auth.AuthStorage.timeStamp"
private const val AUTH_STORAGE_INSTANCE_DOMAIN = "jp.panta.misskeyandroidclient.model.auth.AuthStorage.instanceDomain"
class AuthStorage(private val sharedPreference: SharedPreferences){

    fun setSession(session: Session){
        val editor = sharedPreference.edit()
        editor.putString(AUTH_STORAGE_SESSION_URL, session.url)
        editor.putString(AUTH_STORAGE_SESSION_TOKEN, session.token)
        editor.putLong(AUTH_STORAGE_SESSION_TIME_STAMP, Date().time)
        editor.apply()
    }

    fun getSessionUpdatedAt(): Date?{
        val time = sharedPreference.getLong(AUTH_STORAGE_SESSION_TIME_STAMP, 0)
        if(time == 0L){
            return null
        }
        return Date(time)
    }



    fun getSession(): Session?{
        val token = sharedPreference.getString(AUTH_STORAGE_SESSION_TOKEN, null)
        val url = sharedPreference.getString(AUTH_STORAGE_SESSION_URL, null)
        return if(token != null && url != null){
            Session(token = token, url = url)
        }else{
            null
        }
    }

    fun setInstanceDomain(domain: String){
        val editor = sharedPreference.edit()
        editor.putString(AUTH_STORAGE_INSTANCE_DOMAIN, domain)
        editor.apply()

    }

    fun getInstanceDomain(): String?{
        return sharedPreference.getString(AUTH_STORAGE_INSTANCE_DOMAIN, null)
    }



}