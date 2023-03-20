package net.pantasystem.milktea.api.mastodon.filter

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.filter.MastodonWordFilter

@kotlinx.serialization.Serializable
data class V1FilterDTO(
    @SerialName("id")
    val id: String,

    @SerialName("phrase")
    val phrase: String,

    @SerialName("context")
    val context: List<FilterContext>,

    @SerialName("whole_word")
    val wholeWord: Boolean,

    @SerialName("expires_at")
    val expiresAt: Instant? = null,

    @SerialName("irreversible")
    val irreversible: Boolean,
) {

    fun toModel(account: Account): MastodonWordFilter {
        return MastodonWordFilter(
            MastodonWordFilter.Id(account.accountId, id),
            phrase,
            context = context.map {
                when(it) {
                    FilterContext.Home -> MastodonWordFilter.FilterContext.Home
                    FilterContext.Notifications -> MastodonWordFilter.FilterContext.Notifications
                    FilterContext.Public -> MastodonWordFilter.FilterContext.Public
                    FilterContext.Thread -> MastodonWordFilter.FilterContext.Thread
                    FilterContext.Account -> MastodonWordFilter.FilterContext.Account
                }
            },
            wholeWord,
            expiresAt,
            irreversible
        )

    }
}