package net.pantasystem.milktea.model.instance


data class MastodonInstanceInfo(
    val uri: String,
    val title: String,
    val description: String,
    val email: String,
    val version: String,
    val urls: Urls,
    val configuration: Configuration?,
) {
    companion object;

    data class Configuration(
        val statuses: Statuses?,
        val polls: Polls?
    ) {

        data class Statuses(
            val maxCharacters: Int?,
            val maxMediaAttachments: Int?,
        )

        data class Polls(
            val maxOptions: Int?,
            val maxCharactersPerOption: Int?,
            val minExpiration: Int?,
            val maxExpiration: Int?,
        )

    }

    data class Urls(
        val streamingApi: String?
    )
}
