package jp.panta.misskeyandroidclient.api.auth

import kotlinx.serialization.Serializable

@Serializable data class UserKey(val appSecret: String, val token: String)