package net.pantasystem.milktea.data.infrastructure.filter.db

import androidx.room.Entity
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.filter.MastodonWordFilter

@Entity(
    tableName = "mastodon_word_filters_v1",
    primaryKeys = ["accountId", "filterId"]
)
data class MastodonWordFilterRecord(
    val accountId: Long,
    val filterId: String,
    val phrase: String,
    val wholeWord: Boolean,
    val expiresAt: Instant?,
    val irreversible: Boolean,
    val isContextHome: Boolean,
    val isContextNotifications: Boolean,
    val isContextPublic: Boolean,
    val isContextThread: Boolean,
    val isContextAccount: Boolean,
) {

    fun toModel(): MastodonWordFilter {
        return MastodonWordFilter(
            MastodonWordFilter.Id(accountId, filterId),
            phrase = phrase,
            wholeWord = wholeWord,
            expiresAt = expiresAt,
            irreversible = irreversible,
            context = listOfNotNull(
                if (isContextHome) MastodonWordFilter.FilterContext.Home else null,
                if (isContextNotifications) MastodonWordFilter.FilterContext.Notifications else null,
                if (isContextPublic) MastodonWordFilter.FilterContext.Public else null,
                if (isContextThread) MastodonWordFilter.FilterContext.Thread else null,
                if (isContextAccount) MastodonWordFilter.FilterContext.Account else null,
            )
        )
    }

    companion object {
        fun from(model: MastodonWordFilter): MastodonWordFilterRecord {
            return MastodonWordFilterRecord(
                model.id.accountId,
                model.id.filterId,
                phrase = model.phrase,
                wholeWord = model.wholeWord,
                expiresAt = model.expiresAt,
                irreversible = model.irreversible,
                isContextAccount = model.context.any {
                    it == MastodonWordFilter.FilterContext.Account
                },
                isContextHome = model.context.any {
                    it == MastodonWordFilter.FilterContext.Home
                },
                isContextNotifications = model.context.any {
                    it == MastodonWordFilter.FilterContext.Notifications
                },
                isContextPublic = model.context.any {
                    it == MastodonWordFilter.FilterContext.Public
                },
                isContextThread = model.context.any {
                    it == MastodonWordFilter.FilterContext.Thread
                }
            )
        }
    }
}

