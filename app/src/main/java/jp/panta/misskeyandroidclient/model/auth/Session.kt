package jp.panta.misskeyandroidclient.model.auth

import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import kotlinx.serialization.Serializable

@Serializable data class Session(val token: String, val url: String)