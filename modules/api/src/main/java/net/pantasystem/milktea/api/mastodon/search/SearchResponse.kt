package net.pantasystem.milktea.api.mastodon.search

import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO

@kotlinx.serialization.Serializable
data class SearchResponse(
    @SerialName("accounts")
    val accounts: List<MastodonAccountDTO>,

    @SerialName("statuses")
    val statuses: List<TootStatusDTO>,
)