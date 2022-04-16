package net.pantasystem.milktea.data.api.misskey.auth

import jp.panta.misskeyandroidclient.api.misskey.users.UserDTO
import kotlinx.serialization.Serializable

@Serializable data class AccessToken(val accessToken: String, val user: UserDTO)

