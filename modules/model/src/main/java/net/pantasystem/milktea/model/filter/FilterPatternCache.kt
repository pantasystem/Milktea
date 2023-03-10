package net.pantasystem.milktea.model.filter

import net.pantasystem.milktea.model.account.page.Pageable
import java.util.regex.Pattern
import javax.inject.Inject

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

