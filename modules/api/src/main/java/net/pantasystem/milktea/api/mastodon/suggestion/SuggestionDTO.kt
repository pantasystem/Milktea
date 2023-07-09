package net.pantasystem.milktea.api.mastodon.suggestion

import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO

@kotlinx.serialization.Serializable
data class SuggestionDTO(
    @SerialName("source")
    val source: String,

    @SerialName("account")
    val account: MastodonAccountDTO
)