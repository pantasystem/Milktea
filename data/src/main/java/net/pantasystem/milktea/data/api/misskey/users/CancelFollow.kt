package net.pantasystem.milktea.data.api.misskey.users

import kotlinx.serialization.Serializable

@Serializable
data class CancelFollow (val i: String, val userId: String)