package net.pantasystem.milktea.api.misskey.auth

import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.users.UserDTO

@Serializable data class AccessToken(val accessToken: String, val user: UserDTO)

