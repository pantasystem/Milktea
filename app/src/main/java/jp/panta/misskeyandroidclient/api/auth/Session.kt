package jp.panta.misskeyandroidclient.api.auth

import kotlinx.serialization.Serializable

@Serializable data class Session(val token: String, val url: String)