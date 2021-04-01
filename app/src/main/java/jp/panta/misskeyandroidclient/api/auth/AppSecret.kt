package jp.panta.misskeyandroidclient.api.auth

import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import kotlinx.serialization.Serializable
import retrofit2.Call

@Serializable data class AppSecret (val appSecret: String)