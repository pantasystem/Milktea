package net.pantasystem.milktea.api.mastodon.instance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Instance(
    @SerialName("uri")
    val uri: String,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("email")
    val email: String,

    @SerialName("version")
    val version: String,

    @SerialName("urls")
    val urls: Urls,

    @SerialName("configuration")
    val configuration: Configuration? = null,

    @SerialName("fedibird_capabilities")
    val fedibirdCapabilities: List<String>? = null,

    @SerialName("pleroma")
    val pleroma: Pleroma? = null,

    @SerialName("feature_quote")
    val featureQuote: Boolean? = null,
) {
    @Serializable
    data class Configuration(
        @SerialName("statuses")
        val statuses: Statuses? = null,

        @SerialName("polls")
        val polls: Polls? = null,

        @SerialName("emoji_reactions")
        val emojiReactions: EmojiReactions? = null,
    ) {

        @Serializable
        data class Statuses(
            @SerialName("max_characters") val maxCharacters: Int? = null,
            @SerialName("max_media_attachments") val maxMediaAttachments: Int? = null,
        )

        @Serializable
        data class Polls(
            @SerialName("max_options") val maxOptions: Int? = null,
            @SerialName("max_characters_per_option") val maxCharactersPerOption: Int? = null,
            @SerialName("min_expiration") val minExpiration: Int? = null,
            @SerialName("max_expiration") val maxExpiration: Int? = null,
        )

        @Serializable
        data class EmojiReactions(
            @SerialName("max_reactions") val maxReactions: Int? = null,
            @SerialName("max_reactions_per_account") val maxReactionsPerAccount: Int? = null,
        )

    }


    @Serializable
    data class Urls(
        @SerialName("streaming_api") val streamingApi: String
    )

    @Serializable
    data class Pleroma(
        @SerialName("metadata") val metadata: Metadata,

    ) {
        @Serializable
        data class Metadata(
            @SerialName("features") val features: List<String>,
        )
    }
}
