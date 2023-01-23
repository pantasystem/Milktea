package net.pantasystem.milktea.data.infrastructure.auth.custom

import android.content.Context
import android.content.SharedPreferences
import net.pantasystem.milktea.api.misskey.auth.Session
import net.pantasystem.milktea.common.getPreferenceName
import net.pantasystem.milktea.model.app.AppType
import java.util.*

class CustomAuthStore(private val sharedPreferences: SharedPreferences){

    companion object{
        private const val MISSKEY_SECRET = "jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore.SECRET"
        private const val MISSKEY_SESSION_TOKEN = "jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore.SESSION_TOKEN"
        private const val MISSKEY_SESSION_URL = "jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore.SESSION_URL"
        private const val INSTANCE_DOMAIN = "jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore.INSTANCE_DOMAIN"
        private const val ENABLED_DATE_END = "jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore.ENABLED_DATE_END"
        private const val MISSKEY_VIA_NAME = "jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore.VIA_NAME"


        private const val MASTODON_APP_CLIENT_SECRET = "MASTODON_CLIENT_SECRET"
        private const val MASTODON_SCOPE = "MASTODON_SCOPE"
        private const val REDIRECT_URI = "REDIRECT_URI"
        private const val MASTODON_APP_CLIENT_ID = "CLIENT_ID"
        private const val MASTODON_APP_ID = "MASTODON_APP_ID"
        private const val MASTODON_APP_VAPID_KEY = "MASTODON_VAPID_KEY"
        private const val MASTODON_APP_WEBSITE = "MASTODON_APP_WEBSITE"
        private const val MASTODON_APP_NAME = "MASTODON_APP_NAME"


        private const val TYPE = "TEMPORARILY_AUTH_STATE_TYPE"

        fun newInstance(context: Context): CustomAuthStore{
            return CustomAuthStore(context.getSharedPreferences(context.getPreferenceName(), Context.MODE_PRIVATE))
        }
    }

    fun setCustomAuthBridge(customAuthBridge: TemporarilyAuthState){
        when(customAuthBridge) {
            is TemporarilyAuthState.Mastodon -> {
                sharedPreferences.edit().apply {
                    putString(MASTODON_SCOPE, customAuthBridge.scope)
                    putString(INSTANCE_DOMAIN, customAuthBridge.instanceDomain)
                    putString(REDIRECT_URI, customAuthBridge.app.redirectUri)

                    putLong(ENABLED_DATE_END, customAuthBridge.enabledDateEnd.time)
                    putString(MASTODON_APP_CLIENT_ID, customAuthBridge.app.clientId)
                    putString(MASTODON_APP_CLIENT_SECRET, customAuthBridge.app.clientSecret)
                    putString(MASTODON_APP_ID, customAuthBridge.app.id)
                    putString(MASTODON_APP_NAME, customAuthBridge.app.name)
                    putString(TYPE, "mastodon")
                }.apply()
            }
            is TemporarilyAuthState.Misskey -> {
                sharedPreferences.edit().apply {
                    putString(MISSKEY_SECRET, customAuthBridge.secret)
                    putString(MISSKEY_SESSION_TOKEN, customAuthBridge.session.token)
                    putString(MISSKEY_SESSION_URL, customAuthBridge.session.url)
                    putString(INSTANCE_DOMAIN, customAuthBridge.instanceDomain)
                    putLong(ENABLED_DATE_END, customAuthBridge.enabledDateEnd.time)
                    putString(MISSKEY_VIA_NAME, customAuthBridge.viaName)
                    putString(TYPE, "misskey")
                    apply()
                }
            }
        }


    }

    fun getCustomAuthBridge() : TemporarilyAuthState?{
        sharedPreferences.let {
            val type = it.getString(TYPE, "misskey")
            val instanceDomain = it.getString(INSTANCE_DOMAIN, null)?: return null
            val enabledDate = Date(it.getLong(ENABLED_DATE_END, 0))
            val viaName: String? = it.getString(MISSKEY_VIA_NAME, null)
            if(enabledDate < Date()) {
                return null
            }
            return when (type) {
                "misskey" -> {
                    val secret = it.getString(MISSKEY_SECRET, null)?: return null
                    val sessionToken = it.getString(MISSKEY_SESSION_TOKEN, null)?: return null
                    val sessionUrl = it.getString(MISSKEY_SESSION_URL, null)?: return null
                    TemporarilyAuthState.Misskey(
                        secret = secret,
                        session = Session(
                            url = sessionUrl,
                            token = sessionToken
                        ),
                        instanceDomain = instanceDomain,
                        enabledDateEnd = enabledDate,
                        viaName = viaName
                    )
                }
                "mastodon" -> {
                    TemporarilyAuthState.Mastodon(
                        app = AppType.Mastodon(
                            clientId = it.getString(MASTODON_APP_CLIENT_ID, null)?: return null,
                            clientSecret = it.getString(MASTODON_APP_CLIENT_SECRET, null)?: return null,
                            redirectUri = it.getString(REDIRECT_URI, null)?: return null,
                            id = it.getString(MASTODON_APP_ID, null)?: return null,
                            name = it.getString(MASTODON_APP_NAME, null)?: return null,
                        ),
                        instanceDomain = instanceDomain,
                        enabledDateEnd = enabledDate,
                        scope = it.getString(MASTODON_SCOPE, null)?: return null
                    )
                }
                else -> null
            }

        }
    }
}