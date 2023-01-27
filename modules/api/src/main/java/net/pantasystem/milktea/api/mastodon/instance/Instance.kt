package net.pantasystem.milktea.api.mastodon.instance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Instance(
    val uri: String,
    val title: String,
    val description: String,
    val email: String,
    val version: String,
    val urls: Urls,
    val configuration: Configuration? = null,
) {
    @Serializable
    data class Configuration(
        val statuses: Statuses? = null,
        val polls: Polls? = null
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

    }


    @Serializable
    data class Urls(
        @SerialName("streaming_api") val streamingApi: String
    )
}
