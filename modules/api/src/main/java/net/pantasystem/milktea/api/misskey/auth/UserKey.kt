package net.pantasystem.milktea.api.misskey.auth

import kotlinx.serialization.Serializable

@Serializable data class UserKey(val appSecret: String, val token: String)