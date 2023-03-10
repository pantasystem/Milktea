package net.pantasystem.milktea.model.filter

import android.text.Spanned
import androidx.core.text.parseAsHtml
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import java.util.regex.Pattern
import javax.inject.Inject


class MastodonFilterService @Inject constructor(
    private val patternCache: FilterPatternCache,
    private val getMatchContextFilters: GetMatchContextFilters,
) {
    fun isShouldFilterNote(pageable: Pageable, filters: List<MastodonWordFilter>, note: Note): Boolean {
        val matcher = makeFilter(pageable, filters)?.matcher("")
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
        val contextMatchedFilters = getMatchContextFilters(pageable, filters)
        if (contextMatchedFilters.isEmpty()) return null

        val tokens = contextMatchedFilters
            .map { filterToRegexToken(it) }

        val pattern = Pattern.compile(tokens.joinToString("|"), Pattern.CASE_INSENSITIVE)
        patternCache.put(pageable, filters, pattern)
        return pattern
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
