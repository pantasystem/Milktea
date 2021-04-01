package jp.panta.misskeyandroidclient.api.auth

import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import kotlinx.serialization.Serializable

@Serializable data class Session(val token: String, val url: String)