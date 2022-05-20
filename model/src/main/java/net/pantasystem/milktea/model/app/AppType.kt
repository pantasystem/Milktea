package net.pantasystem.milktea.model.app


sealed interface AppType {
    val callbackUrl: String?
    val secret: String?
    val name: String

    data class Misskey(
        val id: String? = null,
        override val name: String,
        override val callbackUrl: String?,
        val isAuthorized: Boolean? = null,
        val permission: List<String> = emptyList(),
        override val secret: String? = null
    ) : AppType

    companion object;

    data class Mastodon(
        val id: String,
        override val name: String,

        val clientId: String,

        val redirectUri: String,

        val clientSecret: String,

        ) : AppType {
        override val callbackUrl: String
            get() = redirectUri
        override val secret: String
            get() = clientSecret

        companion object
    }
}