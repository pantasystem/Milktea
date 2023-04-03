package net.pantasystem.milktea.data.infrastructure.filter.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.filter.MastodonWordFilter

@Entity(
    tableName = "mastodon_word_filters_v1",
    primaryKeys = ["accountId", "filterId"]
)
data class MastodonWordFilterRecord(
    @ColumnInfo(name = "accountId")
    val accountId: Long,

    @ColumnInfo(name = "filterId")
    val filterId: String,

    @ColumnInfo(name = "phrase")
    val phrase: String,

    @ColumnInfo(name = "wholeWord")
    val wholeWord: Boolean,

    @ColumnInfo(name = "expiresAt")
    val expiresAt: Instant?,

    @ColumnInfo(name = "irreversible")
    val irreversible: Boolean,

    @ColumnInfo(name = "isContextHome")
    val isContextHome: Boolean,

    @ColumnInfo(name = "isContextNotifications")
    val isContextNotifications: Boolean,

    @ColumnInfo(name = "isContextPublic")
    val isContextPublic: Boolean,

    @ColumnInfo(name = "isContextThread")
    val isContextThread: Boolean,

    @ColumnInfo(name = "isContextAccount")
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

