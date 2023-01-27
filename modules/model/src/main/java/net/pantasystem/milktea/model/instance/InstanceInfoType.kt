package net.pantasystem.milktea.model.instance

sealed interface InstanceInfoType {
    data class Misskey(val meta: Meta) : InstanceInfoType
    data class Mastodon(val info: MastodonInstanceInfo) : InstanceInfoType
}