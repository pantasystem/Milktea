package net.pantasystem.milktea.model.instance


data class MastodonInstanceInfo(
    val uri: String,
    val title: String,
    val description: String,
    val email: String,
    val version: String,
    val urls: Urls,
    val configuration: Configuration?,
    val fedibirdCapabilities: List<String>?,
) {
    companion object;

    data class Configuration(
        val statuses: Statuses?,
        val polls: Polls?,
        val emojiReactions: EmojiReactions?,
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

        data class EmojiReactions(
            val maxReactions: Int?,
            val maxReactionsPerAccount: Int?
        )

    }

    data class Urls(
        val streamingApi: String?
    )


    // リアクションを使用可能か？
    val isReactionAvailable: Boolean
        get() = fedibirdCapabilities?.contains("emoji_reaction") ?: false
}
