package jp.panta.misskeyandroidclient.model.auth.custom

import android.content.Context
import android.content.SharedPreferences
import jp.panta.misskeyandroidclient.model.auth.Session
import jp.panta.misskeyandroidclient.util.getPreferenceName
import java.util.*

class CustomAuthStore(private val sharedPreferences: SharedPreferences){

    companion object{
        private const val SECRET = "jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore.SECRET"
        private const val SESSION_TOKEN = "jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore.SESSION_TOKEN"
        private const val SESSION_URL = "jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore.SESSION_URL"
        private const val INSTANCE_DOMAIN = "jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore.INSTANCE_DOMAIN"
        private const val ENABLED_DATE_END = "jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore.ENABLED_DATE_END"
        private const val VIA_NAME = "jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore.VIA_NAME"




        fun newInstance(context: Context): CustomAuthStore{
            return CustomAuthStore(context.getSharedPreferences(context.getPreferenceName(), Context.MODE_PRIVATE))
        }
    }

    fun setCustomAuthBridge(customAuthBridge: CustomAuthBridge){
        sharedPreferences.edit().apply {
            putString(SECRET, customAuthBridge.secret)
            putString(SESSION_TOKEN, customAuthBridge.session.token)
            putString(SESSION_URL, customAuthBridge.session.url)
            putString(INSTANCE_DOMAIN, customAuthBridge.instanceDomain)
            putLong(ENABLED_DATE_END, customAuthBridge.enabledDateEnd.time)
            putString(VIA_NAME, customAuthBridge.viaName)
            apply()
        }

    }

    fun getCustomAuthBridge() : CustomAuthBridge?{
        return sharedPreferences.let {
            val secret = it.getString(SECRET, null)?: return null
            val sessionToken = it.getString(SESSION_TOKEN, null)?: return null
            val sessionUrl = it.getString(SESSION_URL, null)?: return null
            val instanceDomain = it.getString(INSTANCE_DOMAIN, null)?: return null
            val enabledDate = Date(it.getLong(ENABLED_DATE_END, 0))
            val viaName: String? = it.getString(VIA_NAME, null)
            if(enabledDate < Date()) {
                return null
            }
            CustomAuthBridge(
                secret = secret,
                session = Session(url = sessionUrl, token = sessionToken),
                instanceDomain = instanceDomain,
                enabledDateEnd = enabledDate,
                viaName = viaName
            )
        }
    }
}