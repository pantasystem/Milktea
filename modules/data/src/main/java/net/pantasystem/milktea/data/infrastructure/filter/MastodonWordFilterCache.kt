package net.pantasystem.milktea.data.infrastructure.filter

import net.pantasystem.milktea.model.filter.MastodonWordFilter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MastodonWordFilterCache @Inject constructor() {

    private var map = mutableMapOf<Long, List<MastodonWordFilter>>()

    fun put(accountId: Long, filters: List<MastodonWordFilter>) {
        synchronized(this) {
            map[accountId] = filters
        }
    }

    fun get(accountId: Long): List<MastodonWordFilter>? {
        return map[accountId]
    }
}