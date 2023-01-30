package net.pantasystem.milktea.api.mastodon.filter

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
enum class FilterContext {
    @SerialName("home") Home,
    @SerialName("notifications") Notifications,
    @SerialName("public") Public,
    @SerialName("thread") Thread,
    @SerialName("account") Account,

}