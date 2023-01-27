package net.pantasystem.milktea.model.instance

sealed interface InstanceInfoType {
    data class Misskey(val meta: Meta) : InstanceInfoType
    data class Mastodon(val info: MastodonInstanceInfo) : InstanceInfoType

    val iconUrl: String? get() {
        return when(this) {
            is Mastodon -> "https://${info.uri}/favicon.ico"
            is Misskey -> meta.iconUrl
        }
    }

    val uri: String get() {
        return when(this) {
            is Mastodon -> "https://${info.uri}"
            is Misskey -> meta.uri
        }
    }
}