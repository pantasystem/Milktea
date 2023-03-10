package net.pantasystem.milktea.model.filter

import android.text.Spanned
import android.text.TextUtils
import androidx.core.text.parseAsHtml
import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.muteword.WordFilterConfigRepository
import net.pantasystem.milktea.model.user.User
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordFilterService @Inject constructor(
    private val mastodonWordFilterRepository: MastodonWordFilterRepository,
    private val wordFilterConfigRepository: WordFilterConfigRepository,
    private val patternCache: FilterPatternCache,
    private val accountRepository: AccountRepository,
) {
    suspend fun isShouldFilterNote(pageable: Pageable, note: Note?): Boolean {
        note ?: return false
        val account = accountRepository.get(note.id.accountId).getOrNull()
            ?: return false

        if (note.userId == User.Id(account.accountId, account.remoteId)) {
            return false
        }
        val config = wordFilterConfigRepository.get().getOrNull()
        if (config != null) {
            val isMatched = config.checkMatchText(note.text)
                    || config.checkMatchText(note.cw)
                    || note.poll?.let { poll ->
                poll.choices.any { choice ->
                    config.checkMatchText(choice.text)
                }
            } ?: false
            if (isMatched) {
                return true
            }
        }

        if (note.type !is Note.Type.Mastodon) {
            return false
        }

        val filter = mastodonWordFilterRepository.findAll(note.id.accountId).getOrNull()
            ?: return false
        val matcher = makeFilter(pageable, filter)?.matcher("")
            ?: return false

        if (note.poll != null) {
            val pollMatches = note.poll.choices.any { matcher.reset(it.text).find() }
            if (pollMatches) return true
        }

        val spoilerText = note.cw
        return (
                matcher.reset(note.text?.parseAsMastodonHtml().toString()).find() ||
                        (spoilerText?.isNotEmpty() == true && matcher.reset(spoilerText).find())
                )

    }


    private fun makeFilter(pageable: Pageable, filters: List<MastodonWordFilter>): Pattern? {
        val inCache = patternCache.get(pageable, filters)
        if (inCache != null) {
            return inCache
        }
        if (filters.isEmpty()) return null
        val contextMatchedFilters = getMatchContextFilters(filters, pageable)
        if (contextMatchedFilters.isEmpty()) return null



        val tokens = contextMatchedFilters
            .map { filterToRegexToken(it) }

        val pattern = Pattern.compile(TextUtils.join("|", tokens), Pattern.CASE_INSENSITIVE)
        patternCache.put(pageable, filters, pattern)
        return pattern
    }

    private fun getMatchContextFilters(
        filters: List<MastodonWordFilter>,
        pageable: Pageable,
    ): List<MastodonWordFilter> {
        val now = Clock.System.now()
        return filters.filter {
            it.expiresAt == null || it.expiresAt > now
        }.filter { filter ->
            filter.isContextHome && pageable is Pageable.Mastodon.HomeTimeline
                    || filter.isContextNotifications && pageable is Pageable.Notification
                    || filter.isContextPublic && pageable is Pageable.Mastodon.PublicTimeline
                    || filter.isContextPublic && pageable is Pageable.Mastodon.LocalTimeline
                    || filter.isContextAccount && pageable is Pageable.Mastodon.UserTimeline
                    || filter.isContextThread && pageable is Pageable.Show
                    || filter.isContextHome && pageable is Pageable.Mastodon.HomeTimeline
        }
    }

    private fun filterToRegexToken(filter: MastodonWordFilter): String? {
        val phrase = filter.phrase
        val quotedPhrase = Pattern.quote(phrase)
        return if (filter.wholeWord && ALPHANUMERIC.matcher(phrase).matches()) {
            String.format("(^|\\W)%s($|\\W)", quotedPhrase)
        } else {
            quotedPhrase
        }
    }

    companion object {
        private val ALPHANUMERIC = Pattern.compile("^\\w+$")
    }
}


class FilterPatternCache @Inject constructor() {
    data class Key(
        val pageable: Pageable,
        val filters: List<MastodonWordFilter>,
    )
    private val map = mutableMapOf<Key, Pattern>()
    fun get(pageable: Pageable, filters: List<MastodonWordFilter>): Pattern? {
        return map[Key(pageable, filters)]
    }

    fun put(pageable: Pageable, filters: List<MastodonWordFilter>, pattern: Pattern) {
        synchronized(this) {
            map[Key(pageable, filters)] = pattern
        }
    }
}

private fun String.parseAsMastodonHtml(): Spanned {
    return this.replace("<br> ", "<br>&nbsp;")
        .replace("<br /> ", "<br />&nbsp;")
        .replace("<br/> ", "<br/>&nbsp;")
        .replace("  ", "&nbsp;&nbsp;")
        .parseAsHtml()
        /* Html.fromHtml returns trailing whitespace if the html ends in a </p> tag, which
         * most status contents do, so it should be trimmed. */
        .trimTrailingWhitespace()
}

private fun Spanned.trimTrailingWhitespace(): Spanned {
    var i = length
    do {
        i--
    } while (i >= 0 && get(i).isWhitespace())
    return subSequence(0, i + 1) as Spanned
}