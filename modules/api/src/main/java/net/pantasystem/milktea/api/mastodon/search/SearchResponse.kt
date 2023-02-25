package net.pantasystem.milktea.api.mastodon.search

import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO

@kotlinx.serialization.Serializable
data class SearchResponse (
    val accounts: List<MastodonAccountDTO>,
    val statuses: List<TootStatusDTO>,
)