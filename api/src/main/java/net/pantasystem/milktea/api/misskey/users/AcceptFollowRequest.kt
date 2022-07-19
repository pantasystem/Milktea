package net.pantasystem.milktea.api.misskey.users

import kotlinx.serialization.Serializable

@Serializable
data class AcceptFollowRequest (val i: String, val userId: String)