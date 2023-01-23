package net.pantasystem.milktea.api.misskey.auth

import kotlinx.serialization.Serializable

@Serializable data class Session(val token: String, val url: String)