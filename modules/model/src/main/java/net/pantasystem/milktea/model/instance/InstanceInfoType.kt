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

    val maxNoteTextLength: Int get() {
        return when(this) {
            is Mastodon -> info.configuration?.statuses?.maxCharacters ?: 500
            is Misskey -> meta.maxNoteTextLength ?: 3000
        }
    }

    val maxFileCount: Int get() {
        return when(this) {
            is Mastodon -> info.configuration?.statuses?.maxMediaAttachments ?: 4
            is Misskey -> if (meta.getVersion() >= Version("12.100.2")) {
                16
            } else {
                4
            }
        }
    }
}