package jp.panta.misskeyandroidclient.model.auth

import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import retrofit2.Call

data class AppSecret (val appSecret: String){

    fun generateSession(api: MisskeyAPI): Call<Session> {
        return api.generateSession(this)
    }

}