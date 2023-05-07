package net.pantasystem.milktea.api.mastodon.search

import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.api.mastodon.tag.MastodonTagDTO

@kotlinx.serialization.Serializable
data class SearchResponse(
    @SerialName("accounts")
    val accounts: List<MastodonAccountDTO>,

    @SerialName("statuses")
    val statuses: List<TootStatusDTO>,

    @SerialName("hashtags")
    val hashtags: List<MastodonTagDTO>,
)